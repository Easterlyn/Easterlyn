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

		private final AtomicBoolean lock, populated;
		@Getter private final Channel channel;

		public RetentionData(Channel channel) {
			this.channel = channel;
			this.channel.getMessages().setCacheCapacity(MessageList.UNLIMITED_CAPACITY);
			this.lock = new AtomicBoolean();
			this.populated = new AtomicBoolean();
		}

		public void queuePopulation() {
			synchronized (populated) {
				if (populated.get()) {
					return;
				}
				populated.set(true);
				requeuePopulation();
			}
		}

		private void requeuePopulation() {
			getDiscord().queue(new DiscordCallable(CallPriority.LOW) {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
					try {
						if (channel.getMessages().load(100)) {
							requeuePopulation();
						}
					} catch (RateLimitException e) {
						// Re-throw rate limit exceptions, callable will be re-queued
						throw e;
					} catch (Exception e) {
						// Catch and re-throw all other exceptions to ensure that lock does not get stuck
						populated.set(false);
						throw e;
					}
				}
			});
		}

		public void queueDeletion(long retentionDuration) {
			synchronized (lock) {
				if (lock.get()) {
					return;
				}
				lock.set(true);
				requeueDeletion(retentionDuration);
			}
		}

		private void requeueDeletion(long retentionDuration) {
			getDiscord().queue(new DiscordCallable(CallPriority.LOWEST, 1) {
				@Override
				public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
					LocalDateTime retention = LocalDateTime.now().minusSeconds(retentionDuration);
					MessageList list = channel.getMessages();
					if (list.isEmpty()) {
						lock.set(false);
						return;
					}
					int firstDeletableIndex = list.size() - 1;
					for (; firstDeletableIndex >= 0; --firstDeletableIndex) {
						if (list.get(firstDeletableIndex).getTimestamp().isBefore(retention)) {
							// Message not eligible for retention, increment back 
							++firstDeletableIndex;
							break;
						}
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

					boolean complete = endIndex >= list.size();

					list.deleteFromRange(firstDeletableIndex, endIndex);

					if (complete) {
						lock.set(false);
					} else {
						requeueDeletion(retentionDuration);
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
	 * This method is potentially blocking. If the channel data is not populated, it will hang until
	 * 1000 messages have been looked up.
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

		data.queuePopulation();

		data.queueDeletion(duration);
	}

}
