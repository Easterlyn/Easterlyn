package co.sblock.discord.modules;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;

import co.sblock.discord.Discord;
import co.sblock.discord.abstraction.CallPriority;
import co.sblock.discord.abstraction.DiscordCallable;
import co.sblock.discord.abstraction.DiscordModule;

import org.bukkit.configuration.ConfigurationSection;

import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageList;
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
		@Getter private final Channel channel;

		private long nextPopulate;

		public RetentionData(Channel channel) {
			this.channel = channel;
			this.channel.getMessages().setCacheCapacity(MessageList.UNLIMITED_CAPACITY);
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
					channel.getMessages().setCacheCapacity(0);
					channel.getMessages().setCacheCapacity(MessageList.UNLIMITED_CAPACITY);
					queuePopulation(retentionDuration);
					return;
				}

				// If populated, attempt to delete.
				queueDeletion(retentionDuration);
			}
		}

		private void queuePopulation(long retentionDuration) {
			getDiscord().queue(new DiscordCallable(CallPriority.LOWEST) {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
					try {
						final int startSize = channel.getMessages().size();
						if (channel.getMessages().load(100)) {

							// Delete as soon as some messages are loaded, no need to wait.
							queueDeletion(retentionDuration);

							/*
							 * If under 100 messages were added, stop checking. This should not be
							 * affected by deletion, as it all runs on the same thread.
							 */
							if (startSize + 100 > channel.getMessages().size()) {
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

		private void queueDeletion(long retentionDuration) {
			if (this.lockDeletion.get()) {
				return;
			}

			this.lockDeletion.set(true);

			getDiscord().queue(new DiscordCallable(CallPriority.LOW, 1) {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
					LocalDateTime retention = LocalDateTime.now().minusSeconds(retentionDuration);
					MessageList list = channel.getMessages();
					if (list.isEmpty()) {
						lockDeletion.set(false);
						return;
					}

					final ArrayList<IMessage> messages = new ArrayList<>();

					for (int i = list.size() - 1; messages.size() < 100 && i >= 0; --i) {
						IMessage message = list.get(i);

						if (message.getTimestamp().isAfter(retention)) {
							// Message not eligible for retention, search complete.
							break;
						}

						if (!message.isPinned()) {
							// Don't delete pinned messages.
							messages.add(message);
						}
					}

					// No eligible messages.
					if (messages.isEmpty()) {
						lockDeletion.set(false);
						return;
					}

					// Bulk delete requires at least 2 messages.
					if (messages.size() == 1) {
						messages.get(0).delete();
						lockDeletion.set(false);
						return;
					}

					list.bulkDelete(messages);

					// Additional sleep time to prevent rate limiting.
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					lockDeletion.set(false);

					if (!list.isEmpty() && list.get(list.size() - 1).getTimestamp().isAfter(retention)) {
						queueDeletion(retentionDuration);
					}
				}
			});
		}

	}

	private final Map<String, RetentionData> channelData;

	public RetentionModule(Discord discord) {
		super(discord);
		this.channelData = new ConcurrentHashMap<>();
	}

	@Override
	public void doSetup() {
		this.channelData.clear();
	}

	public void setRetention(IGuild guild, Long duration) {
		getDiscord().getDatastore().set("retention." + guild.getID() + ".default", duration);
	}

	public void setRetention(IChannel channel, Long duration) {
		getDiscord().getDatastore().set("retention." + channel.getGuild().getID() + '.' + channel.getID(), duration);
	}

	@Override
	public void doHeartbeat() {
		if (!getDiscord().getDatastore().isConfigurationSection("retention")) {
			return;
		}
		ConfigurationSection retention = getDiscord().getDatastore().getConfigurationSection("retention");
		for (String guildID : retention.getKeys(false)) {
			if (!retention.isConfigurationSection(guildID)) {
				continue;
			}
			IGuild guild = this.getDiscord().getClient().getGuildByID(guildID);
			if (guild == null) {
				continue;
			}
			ConfigurationSection retentionGuild = retention.getConfigurationSection(guildID);
			long defaultRetention = retentionGuild.getLong("default", -1);
			for (IChannel channel : guild.getChannels()) {
				doRetention(channel, retentionGuild.getLong(channel.getID(), defaultRetention));
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
		if (channelData.containsKey(channel.getID())) {
			data = channelData.get(channel.getID());
		} else {
			if (channel == null || channel instanceof IPrivateChannel || channel instanceof IVoiceChannel) {
				return;
			}
			data = new RetentionData((Channel) channel);
			this.channelData.put(channel.getID(), data);
		}

		data.doRetention(duration);
	}

}
