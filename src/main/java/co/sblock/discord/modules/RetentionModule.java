package co.sblock.discord.modules;

import java.time.LocalDateTime;
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

		// Repopulate channels hourly just in case deletion failed.
		private static final long REPOPULATE_AFTER = 3600000;

		private final AtomicBoolean lock, populated;
		@Getter private final Channel channel;

		private long nextPopulate;

		public RetentionData(Channel channel) {
			this.channel = channel;
			this.channel.getMessages().setCacheCapacity(MessageList.UNLIMITED_CAPACITY);
			this.lock = new AtomicBoolean();
			this.populated = new AtomicBoolean();
			this.nextPopulate = System.currentTimeMillis() + REPOPULATE_AFTER;
		}

		public void doRetention(long retentionDuration) {
			synchronized (lock) {
				if (lock.get()) {
					return;
				}
				lock.set(true);
				if (!populated.get() || nextPopulate < System.currentTimeMillis()) {
					populated.set(true);
					nextPopulate = System.currentTimeMillis() + REPOPULATE_AFTER;
					queuePopulation();
					return;
				}
				queueDeletion(retentionDuration);
			}
		}

		private void queuePopulation() {
			getDiscord().queue(new DiscordCallable(CallPriority.LOWEST) {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
					try {
						final int startSize = channel.getMessages().size();
						if (channel.getMessages().load(100)) {
							/*
							 * If under 100 messages were added, stop checking. This can cause
							 * issues with slow responses, but we'll be re-checking hourly.
							 */
							if (startSize + 100 > channel.getMessages().size()) {
								lock.set(false);
								return;
							}

							queuePopulation();
						} else {
							lock.set(false);
						}

						// Additional rate limiting
						Thread.sleep(1500);
					} catch (RateLimitException e) {
						// Re-throw rate limit exceptions, callable will be re-queued
						throw e;
					} catch (InterruptedException e) {
						e.printStackTrace();
						populated.set(false);
						lock.set(false);
					} catch (Exception e) {
						// Catch and re-throw all other exceptions to ensure that lock does not get stuck
						populated.set(false);
						lock.set(false);
						throw e;
					}
				}
			});
		}

		private void queueDeletion(long retentionDuration) {
			getDiscord().queue(new DiscordCallable(CallPriority.LOW, 1) {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
					LocalDateTime retention = LocalDateTime.now().minusSeconds(retentionDuration);
					MessageList list = channel.getMessages();
					if (list.isEmpty()) {
						lock.set(false);
						return;
					}

					// TODO: Don't delete pinned messages

					int firstDeletableIndex = list.size() - 1;
					final int maxFirstIndex = Math.max(0, list.size() - 101);
					for (; firstDeletableIndex >= maxFirstIndex; --firstDeletableIndex) {
						if (list.get(firstDeletableIndex).getTimestamp().isAfter(retention)) {
							// Message not eligible for retention, increment back 
							++firstDeletableIndex;
							break;
						}
					}

					// If all messages are deletable, index will be -1.
					if (firstDeletableIndex < 0) {
						firstDeletableIndex = 0;
					}

					// No eligible messages
					if (firstDeletableIndex >= list.size()) {
						lock.set(false);
						return;
					}

					int endIndex = Math.min(firstDeletableIndex + 100, list.size());

					if (firstDeletableIndex + 1 >= endIndex) {
						list.delete(firstDeletableIndex);
						lock.set(false);
						return;
					}

					list.deleteFromRange(firstDeletableIndex, endIndex);

					// Additional sleep time to prevent rate limiting/missed messages
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (list.isEmpty() || !list.get(list.size() - 1).getTimestamp().isAfter(retention)) {
						lock.set(false);
					} else {
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
