package co.sblock.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;

import co.sblock.chat.channel.CanonNick;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.OfflineUser;

/**
 * A container for all messages sent to <code>Player</code>s from various Chat subsections.
 * 
 * @author Dublek, Jikoo
 */
public class ChatMsgs {

	public static String onChannelJoin(OfflineUser user, Channel channel) {
		String name = user.getPlayer().getDisplayName();
		String message = "pestering";
		ChatColor nameC = ChatColor.GREEN;
		if (channel.hasNick(user)) {
			name = channel.getNick(user);
			if (channel.getType().equals(ChannelType.RP)) {
				nameC = CanonNick.getNick(name).getColor();
				message = CanonNick.getNick(name).getPester();
				name = CanonNick.getNick(name).getHandle();
			}
		}
		return nameC + name + ChatColor.YELLOW + " began "
				+ message + " " + ChatColor.GOLD
				+ channel.getName() + ChatColor.YELLOW + " at "
				+ new SimpleDateFormat("HH:mm").format(new Date());
	}

	public static String onChannelLeave(OfflineUser user, Channel channel) {
		return ChatMsgs.onChannelJoin(user, channel).replaceAll("began", "ceased");
	}

	public static String onChannelSetCurrent(String channelName) {
		return ChatColor.YELLOW + "Current channel set to " + ChatColor.GOLD + channelName;
	}

	public static String onChannelDisband(String channelName) {
		return ChatColor.GOLD + channelName + ChatColor.RED
				+ " has been disbanded! These are indeed dark times...";
	}

	public static String onUserMute(String name) {
		return ChatColor.YELLOW + name + ChatColor.RED + " has been muted in all channels.";
	}

	public static String onUserUnmute(String name) {
		return ChatColor.YELLOW + name + ChatColor.GREEN + " has been unmuted in all channels.";
	}

	public static String onUserKickAnnounce(String userName, String channelName) {
		return ChatColor.YELLOW + userName + " has been kicked from " + ChatColor.GOLD
				+ channelName + ChatColor.YELLOW + "!";
	}

	public static String onUserBanAnnounce(String userName, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.RED + " has been " + ChatColor.BOLD
				+ "banned" + ChatColor.RED + " from " + ChatColor.GOLD + channelName
				+ ChatColor.RED + "!";
	}

	public static String onUserUnbanAnnounce(String userName, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.RED + " has been " + ChatColor.BOLD
				+ "unbanned" + ChatColor.RED + " from " + ChatColor.GOLD + channelName
				+ ChatColor.RED + "!";
	}

	public static String onUserApproved(String userName, String channelName) {
		return ChatColor.YELLOW + userName + " has been approved in "
				+ ChatColor.GOLD + channelName + ChatColor.YELLOW + "!";
	}

	public static String onUserDeapproved(String userName, String channelName) {
		return ChatColor.YELLOW + userName + " has been deapproved in "
				+ ChatColor.GOLD + channelName + ChatColor.YELLOW + "!";
	}

	public static String onChannelModAdd(String userName, String channelName) {
		return ChatColor.YELLOW + userName + " is now a mod in " + ChatColor.GOLD + channelName
				+ ChatColor.YELLOW + "!";
	}

	public static String onUserSetGlobalNick(String userName, String nick) {
		return ChatColor.YELLOW + userName + ChatColor.BLUE + " shall henceforth be know as "
				+ ChatColor.YELLOW + nick;
	}

	public static String onUserRmGlobalNick(String userName, String nick) {
		return ChatColor.YELLOW + userName + ChatColor.BLUE + " is no longer known as "
				+ ChatColor.YELLOW + nick;
	}

	public static String onUserSetNick(String userName, String nick, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.BLUE + " is now known as "
				+ ChatColor.YELLOW + nick + ChatColor.YELLOW + " in " + channelName;
	}

	public static String onUserRmNick(String userName, String nick, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.BLUE + " is no longer known as "
				+ ChatColor.YELLOW + nick + ChatColor.YELLOW + " in " + channelName;
	}

	public static String onChannelCommandFail(String channelName) {
		return ChatColor.RED + "You do not have high enough access in " + ChatColor.GOLD
				+ channelName + ChatColor.RED + " to perform that action!";
	}

	public static String onChannelModRm(String userName, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.RED + " is no longer a mod in " + ChatColor.GOLD
				+ channelName + ChatColor.RED + "!";
	}

	public static String onUserDeniedPrivateAccess(String channelName) {
		return ChatColor.GOLD + channelName + ChatColor.RED + " is a private channel."
				+ ChatColor.YELLOW + " Ask a channel mod for access!";
	}

	public static String errorInvalidChannel(String channelName) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + channelName + ChatColor.RED
				+ " does not exist!";
	}

	public static String unsupportedOperation(String channelName) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + channelName + ChatColor.RED
				+ " does not support that operation.";
	}

	public static String errorInvalidUser(String userName) {
		return ChatColor.YELLOW + userName + ChatColor.RED + " does not exist! Get them to log in.";
	}

	public static String errorNoCurrent() {
		return ChatColor.RED + "You must set a current channel to chat! Use "
				+ ChatColor.AQUA + "/join <channel>";
	}

	public static String errorAlreadyListening(String channelName) {
		return ChatColor.RED + "You are already listening to channel " + ChatColor.GOLD + channelName;
	}

	public static String errorNickNotCanon(String nick) {
		return ChatColor.GOLD + nick + ChatColor.RED + " is not a canon nickname! Use "
				+ ChatColor.AQUA + "/nick list" + ChatColor.RED + " for a list.";
	}

	public static String errorNickInUse(String nick) {
		return ChatColor.GOLD + nick + ChatColor.RED + " is already in use!";
	}

	public static String errorRegionChannelJoin() {
		return ChatColor.RED + "You cannot join a region channel!";
	}

	public static String errorRegionChannelLeave() {
		return ChatColor.RED + "You cannot leave a region channel!";
	}

	public static String errorEmptyMessage() {
		return ChatColor.RED + "You cannot send empty messages!";
	}

	public static String errorDisbandDefault() {
		return ChatColor.RED + "Hardcoded default channels cannot be disbanded.";
	}
}
