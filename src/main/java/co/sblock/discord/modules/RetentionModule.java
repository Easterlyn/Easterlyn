package co.sblock.discord.modules;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.message.BasicNameValuePair;

import org.bukkit.configuration.ConfigurationSection;

import com.google.gson.Gson;

import co.sblock.discord.Discord;
import co.sblock.discord.abstraction.DiscordModule;

import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.Requests;

/**
 * DiscordModule for message retention policies.
 * 
 * @author Jikoo
 */
public class RetentionModule extends DiscordModule {

	private final Map<String, Pair<IMessage, Boolean>> channelRetentionData;
	private final Set<String> channelsUndergoingRetention;

	public RetentionModule(Discord discord) {
		super(discord);
		this.channelRetentionData = new HashMap<>();
		this.channelsUndergoingRetention = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
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
			ConfigurationSection retentionGuild = retention.getConfigurationSection(guildID);
			for (String channelID : retentionGuild.getKeys(false)) {
				if (retentionGuild.isSet(channelID)) {
					doRetention(getDiscord().getClient().getChannelByID(channelID), retentionGuild.getLong(channelID, -1));
				}
			}
		}
	}

	public void setRetention(IGuild guild, IChannel channel, Long duration) {
		if (channelRetentionData.containsKey(channel.getID())) {
			channelRetentionData.remove(channel.getID());
		}
		getDiscord().getDatastore().set("retention." + guild.getID() + '.' + channel.getID(), duration);
	}

	/**
	 * This method is blocking and will run until either the bot is rate limited or messages old
	 * enough have been deleted.
	 * 
	 * @param channel the IChannel to do retention for
	 * @param duration the duration in seconds
	 */
	private void doRetention(IChannel channel, long duration) {
		if (duration == -1 || channel instanceof IVoiceChannel
				|| channel instanceof IPrivateChannel || !(channel instanceof Channel)
				|| channelsUndergoingRetention.contains(channel.getID())) {
			return;
		}

		// Ensure we can read history and delete messages
		try {
			DiscordUtils.checkPermissions(getDiscord().getClient(), channel, EnumSet.of(Permissions.READ_MESSAGE_HISTORY, Permissions.MANAGE_MESSAGES));
		} catch (MissingPermissionsException e) {
			getDiscord().getLogger().warning("Unable to do retention for channel " + channel.mention() + " - cannot read history and delete messages!");
			return;
		}

		/*
		 * Channel history must be populated here. To reduce our usage of Discord's API, we have 2
		 * search modes. When searching backwards, we store the last found valid message and proceed
		 * farther back until we run out of messages. When searching forwards, we use our stored
		 * last valid message to search (if needed) until a timestamp within the retention period is
		 * encountered.
		 */


		// Lock channel as undergoing retention
		this.channelsUndergoingRetention.add(channel.getID());

		List<IMessage> channelHistory = new ArrayList<>();
		LocalDateTime latestAllowedTime = LocalDateTime.now().minusSeconds(duration);
		IMessage earliestRemaining = null, currentTarget = null;
		boolean searchBack = true;
		if (channelRetentionData.containsKey(channel.getID())) {
			Pair<IMessage, Boolean> pair = channelRetentionData.get(channel.getID());
			currentTarget = pair.getLeft();
			searchBack = pair.getRight() || currentTarget != null;
			if (!searchBack && currentTarget.getTimestamp().isBefore(latestAllowedTime)) {
				channelsUndergoingRetention.remove(channel.getID());
				return;
			}
		}

		boolean more = true;
		while (more && channelHistory.size() < 1000) {
			Collection<IMessage> pastMessages;
			try {
				pastMessages = getPastMessages((Channel) channel, currentTarget, searchBack);
			} catch (HTTP429Exception e) {
				System.out.println("Rate limited doing history lookup: " + e.getRetryDelay());
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

			if (currentTarget == null && pastMessages.size() == 0) {
				/*
				 * Channel has no messages.
				 * 
				 * The reason we do not store this data and not re-query is that the vast majority
				 * of channels with retention will not have short enough retention periods to ever
				 * fully drain. The only channel that regularly will be able to is #main at night.
				 * 
				 * Basically, it's a very minor evil that costs me more labor to account for than
				 * it's worth. We make hundreds of Discord queries, one more every few minutes
				 * won't make or break this.
				 */
				break;
			}

			for (IMessage message : pastMessages) {
				LocalDateTime messageTime = message.getTimestamp();
				if (currentTarget == null) {
					currentTarget = message;
				}
				if (searchBack) {
					// Earlier timestamp found, farther back we go.
					if (currentTarget.getTimestamp().isAfter(messageTime)) {
						currentTarget = message;
					}
				}
				// Too old, delete
				if (latestAllowedTime.isAfter(messageTime)) {
					channelHistory.add(message);
					continue;
				}
				if (!searchBack){
					// Message found that is current enough to be kept, no need to keep searching
					more = false;
				}
				// Not too old, set to current if none specified or if older than current
				if (earliestRemaining == null || earliestRemaining.getTimestamp().isAfter(messageTime)) {
					earliestRemaining = message;
					if (!searchBack) {
						currentTarget = message;
					}
					continue;
				}
			}

			// Channel has under 50 messages or search back is complete
			if (pastMessages.size() < 50) {
				if (!searchBack) {
					more = false;
				} else {
					searchBack = false;
				}
			}
		}

		if (currentTarget != null) {
			if (channelHistory.contains(currentTarget)) {
				// Current target is being deleted, use earliest remaining instead to avoid re-determining
				currentTarget = earliestRemaining;
			}
		}

		channelRetentionData.put(channel.getID(), new ImmutablePair<>(currentTarget, searchBack));

		if (channelHistory.size() < 1) {
			channelsUndergoingRetention.remove(channel.getID());
			return;
		}

		// Delete all the messages we've collected
		Iterator<IMessage> iterator = channelHistory.iterator();
		while (iterator.hasNext()) {
			getDiscord().queueMessageDeletion(iterator.next());
		}
	}

	private Collection<IMessage> getPastMessages(Channel channel, IMessage message, boolean back)
			throws HTTP429Exception, DiscordException {
		return getPastMessages(channel, 100, back && message != null ? message.getID() : null, !back
				&& message != null ? message.getID() : null);
	}

	private Collection<IMessage> getPastMessages(Channel channel, @Nullable Integer limit,
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

	private Collection<IMessage> getPastMessages(Channel channel, String request) throws HTTP429Exception, DiscordException {
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
