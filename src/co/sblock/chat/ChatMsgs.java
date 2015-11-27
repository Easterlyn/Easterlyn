package co.sblock.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.sblock.chat.channel.Channel;
import co.sblock.users.User;

/**
 * A container for all messages sent to <code>Player</code>s from various Chat subsections.
 * 
 * @author Dublek, Jikoo
 */
public class ChatMsgs {

	private static final SimpleDateFormat TIME_24 = new SimpleDateFormat("HH:mm");
	private static final String CHANNEL_JOIN = Color.GOOD_PLAYER + "%s" + Color.GOOD
			+ " began pestering " + Color.GOOD_EMPHASIS + "%s" + Color.GOOD + " at %s";
	private static final String ERROR_ALREADY_LISTENING = Color.BAD + "You are already listening to channel " + Color.BAD_EMPHASIS + "%s";
	private static final String ERROR_CURRENT_NULL = Color.BAD + "You must set a current channel to chat! Use "
			+ Color.COMMAND + "/join <channel>";
	private static final String ERROR_DISBAND_DEFAULT = Color.BAD + "Hardcoded default channels cannot be disbanded.";
	private static final String ERROR_EMPTY_MESSAGE = Color.BAD + "You cannot send empty messages!";
	private static final String ERROR_INVALID_CHANNEL = Color.BAD + "Channel " + Color.BAD_EMPHASIS + "%s" + Color.BAD
			+ " does not exist! Did you forget the #?";
	private static final String ERROR_INVALID_USER = Color.BAD_PLAYER + "%s" + Color.BAD + " does not exist! Get them to log in.";
	private static final String ERROR_NICK_NOT_CANON = Color.BAD_EMPHASIS + "%s" + Color.BAD + " is not a canon nickname! Use "
			+ Color.COMMAND + "/nick list" + Color.BAD + " for a list.";
	private static final String ERROR_NICK_TAKEN = Color.BAD_EMPHASIS + "%s" + Color.BAD + " is already in use!";
	private static final String ERROR_REGION_JOIN = Color.BAD + "You cannot join a region channel!";
	private static final String ERROR_REGION_LEAVE = Color.BAD
			+ "You cannot leave a region channel! Use " + Color.COMMAND + "/suppress" + Color.BAD
			+ " to ignore all regional channels.";

	public static String onChannelJoin(User user, Channel channel) {
		return String.format(CHANNEL_JOIN, user.getDisplayName(), channel.getName(), TIME_24.format(new Date()));
	}

	public static String onChannelLeave(User user, Channel channel) {
		return ChatMsgs.onChannelJoin(user, channel).replaceAll("began", "ceased");
	}

	public static String onChannelSetCurrent(String channelName) {
		return Color.GOOD + "Current channel set to " + Color.GOOD_EMPHASIS + channelName;
	}

	public static String onChannelDisband(String channelName) {
		return Color.BAD_EMPHASIS + channelName + Color.BAD
				+ " has been disbanded! These are indeed dark times...";
	}

	public static String onUserMute(String name) {
		return Color.BAD_PLAYER + name + Color.BAD + " has been muted in all channels.";
	}

	public static String onUserUnmute(String name) {
		return Color.GOOD + name + Color.GOOD + " has been unmuted in all channels.";
	}

	public static String onUserKickAnnounce(String userName, String channelName) {
		return Color.BAD_PLAYER + userName + " has been kicked from " + Color.BAD_EMPHASIS
				+ channelName + Color.BAD + "!";
	}

	public static String onUserBanAnnounce(String userName, String channelName) {
		return Color.BAD_PLAYER + userName + Color.BAD + " has been " + Color.BAD_EMPHASIS
				+ "banned" + Color.BAD + " from " + Color.BAD_EMPHASIS + channelName
				+ Color.BAD + "!";
	}

	public static String onUserUnbanAnnounce(String userName, String channelName) {
		return Color.GOOD_PLAYER + userName + Color.GOOD + " has been " + Color.GOOD_EMPHASIS
				+ "unbanned" + Color.GOOD + " from " + Color.GOOD_EMPHASIS + channelName
				+ Color.GOOD + "!";
	}

	public static String onUserApproved(String userName, String channelName) {
		return Color.GOOD + userName + " has been approved in "
				+ Color.GOOD_EMPHASIS + channelName + Color.GOOD + "!";
	}

	public static String onUserDeapproved(String userName, String channelName) {
		return Color.BAD_PLAYER + userName + Color.BAD + " has been deapproved in "
				+ Color.BAD_EMPHASIS + channelName + Color.BAD + "!";
	}

	public static String onChannelModAdd(String userName, String channelName) {
		return Color.GOOD_PLAYER + userName + Color.GOOD + " is now a mod in "
				+ Color.GOOD_EMPHASIS + channelName + Color.GOOD + "!";
	}

	public static String onUserSetNick(String userName, String nick, String channelName) {
		return Color.GOOD_PLAYER + userName + Color.GOOD + " is now known as " + Color.GOOD_PLAYER
				+ nick + Color.GOOD + " in " + Color.GOOD_EMPHASIS + channelName;
	}

	public static String onUserRmNick(String userName, String nick, String channelName) {
		return Color.BAD_PLAYER + userName + Color.BAD + " is no longer known as "
				+ Color.BAD_PLAYER + nick + Color.BAD + " in " + Color.BAD + channelName;
	}

	public static String onChannelCommandFail(String channelName) {
		return Color.BAD + "You do not have high enough access in " + Color.BAD_EMPHASIS
				+ channelName + Color.BAD + " to perform that action!";
	}

	public static String onChannelModRm(String userName, String channelName) {
		return Color.BAD_EMPHASIS + userName + Color.BAD + " is no longer a mod in " + Color.BAD_EMPHASIS
				+ channelName + Color.BAD + "!";
	}

	public static String onUserDeniedPrivateAccess(String channelName) {
		return Color.BAD_EMPHASIS + channelName + Color.BAD + " is a private channel."
				+ Color.GOOD + " Ask a channel mod for access!";
	}

	public static String unsupportedOperation(String channelName) {
		return Color.BAD + "Channel " + Color.BAD_EMPHASIS + channelName + Color.BAD
				+ " does not support that operation.";
	}

	public static String errorAlreadyListening(String channelName) {
		return String.format(ERROR_ALREADY_LISTENING, channelName);
	}

	public static String errorCurrentChannelNull() {
		return ERROR_CURRENT_NULL;
	}

	public static String errorDisbandDefault() {
		return ERROR_DISBAND_DEFAULT;
	}

	public static String errorEmptyMessage() {
		return ERROR_EMPTY_MESSAGE;
	}

	public static String errorInvalidChannel(String channelName) {
		return String.format(ERROR_INVALID_CHANNEL, channelName);
	}

	public static String errorInvalidUser(String userName) {
		return String.format(ERROR_INVALID_USER, userName);
	}

	public static String errorNickNotCanon(String nick) {
		return String.format(ERROR_NICK_NOT_CANON, nick);
	}

	public static String errorNickTaken(String nick) {
		return String.format(ERROR_NICK_TAKEN, nick);
	}

	public static String errorRegionChannelJoin() {
		return ERROR_REGION_JOIN;
	}

	public static String errorRegionChannelLeave() {
		return ERROR_REGION_LEAVE;
	}
}
