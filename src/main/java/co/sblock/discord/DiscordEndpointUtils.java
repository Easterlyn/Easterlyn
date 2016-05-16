package co.sblock.discord;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import org.json.simple.JSONObject;

import com.google.gson.Gson;

import co.sblock.discord.abstraction.CallPriority;
import co.sblock.discord.abstraction.DiscordCallable;

import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * A utility for accessing certain Discord endpoints not exposed by Discord4J.
 * <p/>
 * As I'm not sure where these would fit in with the Discord4J API design, I'd prefer to keep them
 * separate.
 * 
 * @author Jikoo
 */
public class DiscordEndpointUtils {

	private DiscordEndpointUtils() {}

	/**
	 * Get past messages for a Channel.
	 * 
	 * @param discord the Discord Module
	 * @param channel the channel
	 * @param message the message, or null to load from the start
	 * @param back whether to search back or forwards
	 * @return
	 * @throws HTTP429Exception if rate limited
	 * @throws DiscordException if an exception occurs
	 */
	public static List<IMessage> getPastMessages(Discord discord, Channel channel, @Nullable IMessage message, boolean back)
			throws HTTP429Exception, DiscordException {
		return getPastMessages(discord, channel, 100, back && message != null ? message.getID() : null, !back
				&& message != null ? message.getID() : null);
	}

	/**
	 * Get past messages for a Channel.
	 * 
	 * @param discord the Discord Module
	 * @param channel the channel
	 * @param limit the number of messages to retrieve, default 50, maximum 100
	 * @param before the message ID to fetch messages before
	 * @param after the message ID to fetch messages after
	 * @return
	 * @throws HTTP429Exception if rate limited
	 * @throws DiscordException if an exception occurs
	 */
	public static List<IMessage> getPastMessages(Discord discord, Channel channel, @Nullable Integer limit,
			@Nullable String before, @Nullable String after)
					throws HTTP429Exception, DiscordException {
		StringBuilder request = new StringBuilder(DiscordEndpoints.CHANNELS)
				.append(channel.getID()).append("/messages");
		if (limit == null && before == null && after == null) {
			if (channel.getMessages().size() >= 50) {
				// 50 is the default size returned. With no parameters, don't bother with a lookup.
				return channel.getMessages();
			} else {
				return getPastMessages(discord, channel, request.toString());
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
		return getPastMessages(discord, channel, request.append(options).toString());
	}

	private static List<IMessage> getPastMessages(Discord discord, Channel channel, String request) throws HTTP429Exception, DiscordException {
		List<IMessage> messages = new ArrayList<>();
		String response;
		response = Requests.GET.makeRequest(request, new BasicNameValuePair("authorization", discord.getClient().getToken()));

		if (response == null) {
			return messages;
		}

		MessageResponse[] msgs = new Gson().fromJson(response, MessageResponse[].class);

		for (MessageResponse message : msgs) {
			IMessage msg = DiscordUtils.getMessageFromJSON(discord.getClient(), channel, message);
			//channel.addMessage(msg);
			messages.add(msg);
		}

		return messages;
	}

	@SuppressWarnings("unchecked")
	public static void queueBulkDelete(Discord discord, CallPriority priority, List<IMessage> messages) {
		if (messages.size() < 2) {
			throw new IllegalArgumentException("Cannot bulk delete under 2 messages!");
		}

		if (messages.size() > 100) {
			throw new IllegalArgumentException("Cannot bulk delete over 100 messages!");
		}

		String channelID = messages.get(0).getChannel().getID();
		ArrayList<String> messageIDs = new ArrayList<>(messages.size());
		for (IMessage message : messages) {
			if (!message.getChannel().getID().equals(channelID)) {
				throw new IllegalArgumentException("Bulk deletion requires all messsages to be in the same channel!");
			}
			messageIDs.add(message.getID());
		}
		JSONObject body = new JSONObject();
		body.put("messages", messageIDs);
		StringEntity entity;
		try {
			entity = new StringEntity(body.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		discord.queue(new DiscordCallable(priority, 0) {
			@Override
			public void call() throws MissingPermissionsException, HTTP429Exception, DiscordException {

				Requests.POST.makeRequest(DiscordEndpoints.CHANNELS + channelID + "/messages/bulk_delete", entity,
						new BasicNameValuePair("authorization", discord.getClient().getToken()),
						new BasicNameValuePair("content-type", "application/json"));
			}
		});
	}

//	public static JSONObject getGuildMember(Discord discord, IGuild guild, IUser user) throws HTTP429Exception, DiscordException, ParseException {
//		String response = Requests.GET.makeRequest(DiscordEndpoints.GUILDS + guild.getID() + "/members/" + user.getID(),
//				new BasicNameValuePair("authorization", discord.getClient().getToken()));
//		if (response == null) {
//			return null;
//		}
//
//		return (JSONObject) new JSONParser().parse(response);
//	}

	/**
	 * Queue a nickname change for a user.
	 * 
	 * @param discord the Discord Module
	 * @param priority the CallPriority to queue the DiscordCallable with
	 * @param guild the IGuild
	 * @param user the IUser whose nick should be changed
	 * @param name the nickname to set
	 * @throws MissingPermissionsException if not allowed to change nicknames
	 */
	@SuppressWarnings("unchecked")
	public static void queueNickSet(Discord discord, CallPriority priority, IGuild guild, IUser user, String name)
			throws MissingPermissionsException {

		// TODO: Discord4J does not yet support new permissions
//		if (discord.getClient().getOurUser().equals(user)) {
//			DiscordUtils.checkPermissions(discord.getClient(), guild, EnumSet.of(Permissions.CHANGE_NICKNAME));
//		} else {
//		DiscordUtils.checkPermissions(discord.getClient(), guild, EnumSet.of(Permissions.MANAGE_NICKNAMES));
//		}
		DiscordUtils.checkPermissions(discord.getClient(), guild, EnumSet.of(Permissions.MANAGE_SERVER));

		JSONObject body = new JSONObject();
		body.put("nick", name);
		StringEntity entity;
		try {
			entity = new StringEntity(body.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		discord.queue(new DiscordCallable(priority, 0) {
			@Override
			public void call() throws MissingPermissionsException, HTTP429Exception, DiscordException {
				Requests.PATCH.makeRequest(DiscordEndpoints.GUILDS + guild.getID() + "/members/" + user.getID(),
						entity, new BasicNameValuePair("authorization", discord.getClient().getToken()),
						new BasicNameValuePair("content-type", "application/json"));
			}
		});
	}

}
