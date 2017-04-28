package com.easterlyn.discord.modules;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.easterlyn.discord.Discord;
import com.easterlyn.discord.abstraction.DiscordModule;
import com.easterlyn.discord.queue.CallPriority;
import com.easterlyn.discord.queue.CallType;
import com.easterlyn.discord.queue.DiscordCallable;
import com.koloboke.function.LongObjPredicate;

import org.apache.http.message.BasicNameValuePair;

import org.bukkit.configuration.ConfigurationSection;

import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.MessageObject;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * DiscordModule for message retention policies.
 * 
 * @author Jikoo
 */
public class RetentionModule extends DiscordModule {

	private class RetentionData {

		// Repopulate channels every 6 hours in case deletion failed.
		private static final long REPOPULATE_AFTER = 21600000;

		private final AtomicBoolean lockDeletion, lockPopulation;
		private final Channel channel;

		private long nextPopulate;

		public RetentionData(Channel channel) {
			this.channel = channel;
			this.lockDeletion = new AtomicBoolean();
			this.lockPopulation = new AtomicBoolean();
			this.nextPopulate = System.currentTimeMillis() + REPOPULATE_AFTER;
		}

		public void doRetention(long retentionDuration) {
			synchronized (lockDeletion) {
				if (lockDeletion.get() && lockPopulation.get()) {
					return;
				}

				// Data populated?
				if (!lockPopulation.get() || nextPopulate < System.currentTimeMillis()) {
					lockPopulation.set(true);
					nextPopulate = System.currentTimeMillis() + REPOPULATE_AFTER;
					this.channel.messages.clear();
					queuePopulation(retentionDuration);
					return;
				}

				// If populated, attempt to delete.
				queueDeletion(retentionDuration);
			}
		}

		private void queuePopulation(long retentionDuration) {
			getDiscord().queue(new DiscordCallable(this.channel.getGuild().getLongID(), CallType.MESSAGE_POPULATE) {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
					try {
						final int startSize = channel.getInternalCacheCount();
						Long before = null;
						synchronized (channel.messages) {
							if (channel.messages.size() > 0) {
								before = channel.messages.stream().mapToLong(message -> message.getLongID()).min().getAsLong();
							}
						}
						if (addHistory(before, 100).length > 0) {

							// Delete as soon as some messages are loaded, no need to wait.
							queueDeletion(retentionDuration);

							/*
							 * If under 100 messages were added, stop checking. This should not be
							 * affected by deletion, as it all runs on the same thread.
							 */
							if (startSize + 100 > channel.getInternalCacheCount()) {
								return;
							}

							queuePopulation(retentionDuration);
						}

						// Additional rate limiting prevention
						Thread.sleep(2000);

					} catch (RateLimitException e) {
						// Re-throw rate limit exceptions, callable will be re-queued
						throw e;
					} catch (InterruptedException e) {
						e.printStackTrace();
						lockPopulation.set(false);
					} catch (Exception e) {
						// Catch and re-throw all other exceptions to ensure that lock does not get stuck
						lockPopulation.set(false);
						throw e;
					}
				}
			});
		}

		/**
		 * @see {@link sx.blah.discord.handle.impl.obj.Channel#requestHistory(Long before, int limit)}
		 */
		private IMessage[] addHistory(Long before, int limit) {
			DiscordUtils.checkPermissions(this.channel.getClient(), this.channel,
					EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY));
			String queryParams = "?limit=" + limit;
			if (before != null) {
				queryParams = queryParams + "&before=" + Long.toUnsignedString(before.longValue());
			}

			MessageObject[] messages = ((DiscordClientImpl) this.channel.getClient()).REQUESTS.GET
					.makeRequest(DiscordEndpoints.CHANNELS + this.channel.getLongID() + "/messages" + queryParams,
							MessageObject[].class, new BasicNameValuePair[0]);
			if (messages.length == 0) {
				return new IMessage[0];
			} else {
				IMessage[] messageObjs = new IMessage[messages.length];

				synchronized (this.channel.messages) { // Jikoo: Prevent potential CME if message is cached
					for (int i = 0; i < messages.length; ++i) {
						IMessage message = DiscordUtils.getMessageFromJSON(this.channel, messages[i]);
						if (message != null) {
							messageObjs[i] = message;
							this.channel.messages.putIfAbsent(message.getLongID(), () -> message);
						}
					}
				}

				return messageObjs;
			}
		}

		private void queueDeletion(long retentionDuration) {
			if (this.lockDeletion.get() || this.channel.getInternalCacheCount() == 0) {
				return;
			}

			this.lockDeletion.set(true);

			getDiscord().queue(new DiscordCallable(this.channel.getGuild().getLongID(), CallType.BULK_DELETE) {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
					LocalDateTime retention = LocalDateTime.now().minusSeconds(retentionDuration);
					if (channel.getInternalCacheCount() < 1) {
						lockDeletion.set(false);
						return;
					}

					final ArrayList<IMessage> messages = new ArrayList<>();

					LocalDateTime bulkDeleteableBefore = LocalDateTime.now().plusDays(13).plusHours(12);

					channel.messages.forEachWhile(new LongObjPredicate<IMessage>(){
						@Override
						public boolean test(long messageID, IMessage message) {
							if (message.isPinned() || message.isDeleted()|| message.getTimestamp().isAfter(retention)) {
								// Message not eligible for retention.
								return false;
							}
							// FIXME Discord is throwing its 14 days or less on most bulk deletes
							if (true || message.getTimestamp().isAfter(bulkDeleteableBefore)) {
								// Message is too old to be bulk deleted. Queue at LOWEST so bulk deletes run sooner.
								getDiscord().queueMessageDeletion(CallPriority.LOWEST, message);
								return false;
							}

							messages.add(message);
							return messages.size() > 99;
						}
					});

					// No eligible messages.
					if (messages.isEmpty()) {
						lockDeletion.set(false);
						return;
					}

					// Bulk delete requires at least 2 messages.
					if (messages.size() == 1) {
						try {
							messages.get(0).delete();
						} catch (Exception e) {
							if (!(e instanceof RateLimitException)) {
								lockDeletion.set(false);
							}
							throw e;
						}
						lockDeletion.set(false);
						return;
					}

					try {
						channel.bulkDelete(messages);
					} catch (Exception e) {
						if (!(e instanceof RateLimitException)) {
							lockDeletion.set(false);
						}
						throw e;
					}

					// Additional sleep time to prevent rate limiting.
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					lockDeletion.set(false);


					Long before = null;
					synchronized (channel.messages) {
						if (channel.messages.size() > 0) {
							before = channel.messages.stream().mapToLong(message -> message.getLongID()).min().getAsLong();
						}
					}

					if (before != null && channel.messages.get(before).getTimestamp().isAfter(retention)) {
						queueDeletion(retentionDuration);
					}
				}
			}.withRetries(1));
		}

	}

	private final Map<Long, RetentionData> channelData;

	public RetentionModule(Discord discord) {
		super(discord);
		this.channelData = new ConcurrentHashMap<>();
	}

	@Override
	public void doSetup() {
		this.channelData.clear();
	}

	public void setRetention(IGuild guild, Long duration) {
		synchronized (getDiscord().getDatastore()) {
			getDiscord().getDatastore().set("retention." + guild.getLongID() + ".default", duration);
		}
	}

	public void setRetention(IChannel channel, Long duration) {
		synchronized (getDiscord().getDatastore()) {
			getDiscord().getDatastore().set("retention." + channel.getGuild().getLongID() + '.' + channel.getLongID(), duration);
		}
	}

	@Override
	public void doHeartbeat() {
		if (!getDiscord().getDatastore().isConfigurationSection("retention")) {
			return;
		}
		ConfigurationSection retention = getDiscord().getDatastore().getConfigurationSection("retention");
		for (String guildIDString : retention.getKeys(false)) {
			if (!retention.isConfigurationSection(guildIDString)) {
				continue;
			}
			long guildID;
			try {
				guildID = Long.valueOf(guildIDString);
			} catch (NumberFormatException e) {
				// Invalid ID
				continue;
			}
			IGuild guild = this.getDiscord().getClient().getGuildByID(guildID);
			if (guild == null) {
				continue;
			}
			ConfigurationSection retentionGuild = retention.getConfigurationSection(guildIDString);
			long defaultRetention = retentionGuild.getLong("default", -1);
			for (IChannel channel : guild.getChannels()) {
				doRetention(channel, retentionGuild.getLong(String.valueOf(channel.getLongID()), defaultRetention));
			}
		}
	}

	/**
	 * Queue population and deletion for a channel.
	 * 
	 * @param channel the IChannel to do retention for
	 * @param duration the duration in seconds
	 */
	private void doRetention(IChannel channel, long duration) {
		if (duration == -1 || channel instanceof IVoiceChannel
				|| channel instanceof IPrivateChannel || !(channel instanceof Channel)) {
			return;
		}

		// Ensure we can read history and delete messages
		try {
			DiscordUtils.checkPermissions(getDiscord().getClient(), channel, EnumSet.of(Permissions.READ_MESSAGE_HISTORY, Permissions.MANAGE_MESSAGES));
		} catch (MissingPermissionsException e) {
			getDiscord().getLogger().warning("Unable to do retention for channel " + channel.mention() + " - cannot read history and delete messages!");
			return;
		}

		RetentionData data;
		if (channelData.containsKey(channel.getLongID())) {
			data = channelData.get(channel.getLongID());
		} else {
			if (channel == null || channel instanceof IPrivateChannel || channel instanceof IVoiceChannel) {
				return;
			}
			data = new RetentionData((Channel) channel);
			this.channelData.put(channel.getLongID(), data);
		}

		data.doRetention(duration);
	}

}
