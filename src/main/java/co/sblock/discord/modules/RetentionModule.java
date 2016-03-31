package co.sblock.discord.modules;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import lombok.Getter;
import lombok.Setter;

import org.apache.http.message.BasicNameValuePair;

import org.bukkit.configuration.ConfigurationSection;

import com.google.gson.Gson;

import co.sblock.discord.Discord;
import co.sblock.discord.abstraction.CallPriority;
import co.sblock.discord.abstraction.DiscordCallable;
import co.sblock.discord.abstraction.DiscordModule;

import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * DiscordModule for message retention policies.
 * 
 * @author Jikoo
 */
public class RetentionModule extends DiscordModule {

	private class RetentionData {

		private final AtomicBoolean lock;
		@Getter private final Channel channel;
		@Getter private final List<IMessage> messages;
		@Getter @Setter private boolean fullyPopulated;

		public RetentionData(Channel channel) {
			this.channel = channel;
			this.messages = new LinkedList<>();
			this.fullyPopulated = false;
			this.lock = new AtomicBoolean();
		}

		public boolean isLocked() {
			return this.lock.get();
		}

		public void setLocked(boolean value) {
			this.lock.set(value);
		}

		public void addMessage(IMessage message) {
			synchronized (lock) {
				this.messages.add(0, message);
			}
		}

		public void addMessages(Collection<IMessage> messages) {
			synchronized (lock) {
				this.messages.addAll(messages);
			}
		}

		public void removeDeletedMessage(IMessage message) {
			synchronized (lock) {
				this.messages.remove(message);
			}
		}

		public void sortMessages() {
			synchronized (lock) {
				Collections.sort(this.messages, (IMessage msg1, IMessage msg2) ->
						msg2.getTimestamp().compareTo(msg1.getTimestamp()));
			}
		}

		public IMessage getEarliestMessage() {
			synchronized (lock) {
				return this.messages.isEmpty() ? null : this.messages.get(this.messages.size() - 1);
			}
		}

		public IMessage removeEarliestMessageIfBefore(LocalDateTime time) {
			synchronized (lock) {
				if (messages.isEmpty()) {
					return null;
				}
				int index = messages.size() - 1;
				if (messages.get(index).getTimestamp().isBefore(time)) {
					return messages.remove(index);
				}
				return null;
			}
		}

	}

	private final Map<String, RetentionData> channelData;

	public RetentionModule(Discord discord) {
		super(discord);
		this.channelData = new ConcurrentHashMap<>();
	}

	@Override
	public void doSetup() { }

	public void setRetention(IGuild guild, Long duration) {
		getDiscord().getDatastore().set("retention." + guild.getID() + ".default", duration);
	}

	public void setRetention(IChannel channel, Long duration) {
		getDiscord().getDatastore().set("retention." + channel.getGuild().getID() + '.' + channel.getID(), duration);
	}

	public void handleMessageDelete(IMessage message) {
		String channelID = message.getChannel().getID();
		if (channelData.containsKey(channelID)) {
			channelData.get(channelID).removeDeletedMessage(message);
		}
	}

	public void handleNewMessage(IMessage message) {
		String channelID = message.getChannel().getID();
		if (channelData.containsKey(channelID)) {
			channelData.get(channelID).addMessage(message);
		}
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
				retention.set(guildID, null);
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

		// Channel is already undergoing retention, return.
		if (data.isLocked()) {
			return;
		}

		// Lock while we look up past messages and potentially while we do retention later
		data.setLocked(true);

		// Populate up to 1000 past messages
		this.populateChannel(data);

		LocalDateTime retention = LocalDateTime.now().minusSeconds(duration);

		// If channel has no deletion-eligible messages, unlock and return.
		IMessage earliest = data.getEarliestMessage();
		if (earliest == null && data.isFullyPopulated()
				|| earliest != null && retention.isBefore(earliest.getTimestamp())) {
			data.setLocked(false);
			return;
		}

		IMessage message = null;
		while ((message = data.removeEarliestMessageIfBefore(retention)) != null) {
			this.getDiscord().queueMessageDeletion(message, CallPriority.LOW);
		}

		/*
		 * Queue retention unlock. We use CallPriority.LOWEST so that all retention deletion has a
		 * chance to complete before we start another cycle. Sure, it deletes backed up old messages
		 * a little slower, but it also more evenly distributes deletion.
		 */
		this.getDiscord().queue(new DiscordCallable(CallPriority.LOWEST) {
			@Override
			public void call() throws DiscordException, HTTP429Exception, MissingPermissionsException {
				data.setLocked(false);
			}
		});
	}

	private void populateChannel(RetentionData data) {
		if (data.isFullyPopulated()) {
			return;
		}

		// Load a maximum of 1000 messages per iteration
		for (int i = 0; i < 10 && !data.isFullyPopulated(); ++i) {

			List<IMessage> pastMessages;
			try {
				pastMessages = getPastMessages(data.getChannel(), data.getEarliestMessage(), true);
			} catch (HTTP429Exception e) {
				this.getDiscord().getLogger().warning("Rate limited doing history lookup: " + e.getRetryDelay());
				// Decrease total try count
				--i;
				try {
					Thread.sleep(e.getRetryDelay());
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				continue;
			} catch (DiscordException e) {
				/*
				 * An error occurred fetching past messages. This usually means the message ID being
				 * used to search is invalid or there are no messages beyond the one specified.
				 */
				break;
			}

			if (pastMessages.size() < 50) {
				data.setFullyPopulated(true);
			}

			data.addMessages(pastMessages);
		}

		// There's no guarantee that Discord will return messages in any order.
		// While they currently do, that may change. To be safe, we sort the messages ourselves.
		data.sortMessages();
	}

	private List<IMessage> getPastMessages(Channel channel, IMessage message, boolean back)
			throws HTTP429Exception, DiscordException {
		return getPastMessages(channel, 100, back && message != null ? message.getID() : null, !back
				&& message != null ? message.getID() : null);
	}

	private List<IMessage> getPastMessages(Channel channel, @Nullable Integer limit,
			@Nullable String before, @Nullable String after)
					throws HTTP429Exception, DiscordException {
		StringBuilder request = new StringBuilder(DiscordEndpoints.CHANNELS)
				.append(channel.getID()).append("/messages");
		if (limit == null && before == null && after == null) {
			if (channel.getMessages().size() >= 50) {
				// 50 is the default size returned. With no parameters, don't bother with a lookup.
				return channel.getMessages();
			} else {
				return getPastMessages(channel, request.toString());
			}
		}
		StringBuilder options = new StringBuilder("?");
		if (limit != null) {
			options.append("limit=").append(limit);
		}
		if (before != null) {
			if (options.length() > 1) {
				options.append('&');
			}
			options.append("before=").append(before);
		}
		if (after != null) {
			if (options.length() > 1) {
				options.append('&');
			}
			options.append("after=").append(after);
		}
		return getPastMessages(channel, request.append(options).toString());
	}

	private List<IMessage> getPastMessages(Channel channel, String request) throws HTTP429Exception, DiscordException {
		List<IMessage> messages = new ArrayList<>();
		String response;
		response = Requests.GET.makeRequest(request, new BasicNameValuePair("authorization", getDiscord().getClient().getToken()));

		if (response == null) {
			return messages;
		}

		MessageResponse[] msgs = new Gson().fromJson(response, MessageResponse[].class);

		for (MessageResponse message : msgs) {
			IMessage msg = DiscordUtils.getMessageFromJSON(getDiscord().getClient(), channel, message);
			//channel.addMessage(msg);
			messages.add(msg);
		}

		return messages;
	}

}
