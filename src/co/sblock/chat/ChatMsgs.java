package co.sblock.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.sblock.chat.channel.Channel;
import co.sblock.users.OfflineUser;

import net.md_5.bungee.api.ChatColor;


/**
 * A container for all messages sent to <code>Player</code>s from various Chat subsections.
 * 
 * @author Dublek, Jikoo
 */
public class ChatMsgs {

	public static String onChannelJoin(OfflineUser user, Channel channel) {
		String name = user.getPlayer().getDisplayName();
		String message = "pestering";
		ChatColor nameC = Color.GOOD_PLAYER;
		if (channel.hasNick(user)) {
			name = channel.getNick(user);
		}
		return nameC + name + Color.GOOD + " began " + message + " " + Color.GOOD_EMPHASIS
				+ channel.getName() + Color.GOOD + " at "
				+ new SimpleDateFormat("HH:mm").format(new Date());
	}

	public static String onChannelLeave(OfflineUser user, Channel channel) {
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

	public static String errorInvalidChannel(String channelName) {
		return Color.BAD + "Channel " + Color.BAD_EMPHASIS + channelName + Color.BAD
				+ " does not exist!";
	}

	public static String unsupportedOperation(String channelName) {
		return Color.BAD + "Channel " + Color.BAD_EMPHASIS + channelName + Color.BAD
				+ " does not support that operation.";
	}

	public static String errorInvalidUser(String userName) {
		return Color.BAD_PLAYER + userName + Color.BAD + " does not exist! Get them to log in.";
	}

	public static String errorNoCurrent() {
		return Color.BAD + "You must set a current channel to chat! Use "
				+ Color.COMMAND + "/join <channel>";
	}

	public static String errorAlreadyListening(String channelName) {
		return Color.BAD + "You are already listening to channel " + Color.BAD_EMPHASIS + channelName;
	}

	public static String errorNickNotCanon(String nick) {
		return Color.BAD_EMPHASIS + nick + Color.BAD + " is not a canon nickname! Use "
				+ Color.COMMAND + "/nick list" + Color.BAD + " for a list.";
	}

	public static String errorNickInUse(String nick) {
		return Color.BAD_EMPHASIS + nick + Color.BAD + " is already in use!";
	}

	public static String errorRegionChannelJoin() {
		return Color.BAD + "You cannot join a region channel!";
	}

	public static String errorRegionChannelLeave() {
		return Color.BAD + "You cannot leave a region channel!";
	}

	public static String errorEmptyMessage() {
		return Color.BAD + "You cannot send empty messages!";
	}

	public static String errorDisbandDefault() {
		return Color.BAD + "Hardcoded default channels cannot be disbanded.";
	}
}
