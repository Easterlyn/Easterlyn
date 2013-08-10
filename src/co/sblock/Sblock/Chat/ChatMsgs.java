package co.sblock.Sblock.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;

import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.Nick;
import co.sblock.Sblock.UserData.SblockUser;

public class ChatMsgs {

	public static String onChannelJoin(SblockUser u, Channel c) {
		Nick n = c.getNick(u);
		return n.getColor() + n.getName() + ChatColor.YELLOW + " began "
				+ c.getNick(u).getPester() + " " + ChatColor.GOLD
				+ c.getName() + ChatColor.YELLOW + " at "
				+ new SimpleDateFormat("HH:mm").format(new Date());
	}

	public static String onChannelLeave(SblockUser u, Channel c) {
		return ChatMsgs.onChannelJoin(u, c).replaceAll("began", "ceased");
	}

	public static String onChannelDisband(String c) {
		return ChatColor.GOLD + c + ChatColor.RED
				+ " has been disbanded! These are indeed dark times...";
	}

	public static String onUserMute(SblockUser u) {
		return ChatColor.RED + "You have been muted in all channels.";
	}

	public static String onUserUnmute(SblockUser u) {
		return ChatColor.GREEN + "You have been unmuted in all channels.";
	}

	public static String isMute(SblockUser u, Channel c) {
		return ChatColor.RED + "You are muted in channel " + ChatColor.GOLD
				+ c.getName() + ChatColor.RED + "!";
	}

	public static String onUserKick(Channel c) {
		return ChatColor.YELLOW + "You have been kicked from "
				+ ChatColor.GOLD + c.getName() + ChatColor.YELLOW + "!";
	}

	public static String onUserKickAnnounce(SblockUser u, Channel c) {
		return ChatColor.YELLOW + u.getPlayerName() + " has been kicked from "
				+ ChatColor.GOLD + c.getName() + ChatColor.YELLOW + "!";
	}

	public static String onUserKickFail(Channel c) {
		return ChatColor.RED + "You do not have permission to kick people in "
				+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!";
	}

	public static String onUserKickedAlready(SblockUser u, Channel c) {
		return ChatColor.YELLOW + u.getPlayerName() + ChatColor.RED +
				" is not chatting in " + ChatColor.GOLD + c.getName() +
				ChatColor.RED + "!";
	}

	public static String onUserBan(Channel c) {
		return ChatColor.RED + "You have been " + ChatColor.BOLD
				+ "banned" + ChatColor.RESET + ChatColor.RED + " from "
				+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!";
	}

	public static String onUserBanAnnounce(String u, Channel c) {
		return ChatColor.YELLOW + u + ChatColor.RED
				+ " has been " + ChatColor.BOLD + "banned"
				+ ChatColor.RESET + " from " + ChatColor.GOLD
				+ c.getName() + ChatColor.RED + "!";
	}

	public static String onUserBanFail(Channel c) {
		return ChatColor.RED + "You do not have permission to ban people in "
				+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!";
	}

	public static String onUserBannedAlready(String u, Channel c) {
		return ChatColor.YELLOW + u + ChatColor.RED +
				" is already banned in " + ChatColor.GOLD
				+ c.getName() + ChatColor.RED + "!";
	}

	public static String onUserUnban(Channel c) {
		return ChatColor.RED + "You have been " + ChatColor.BOLD
				+ "unbanned" + ChatColor.RESET + " from " + ChatColor.GOLD
				+ c.getName() + ChatColor.RED + "!";
	}

	public static String onUserUnbanAnnounce(String u, Channel c) {
		return ChatColor.YELLOW + u + ChatColor.RED +
				" has been " + ChatColor.BOLD + "unbanned" + ChatColor.RESET +
				" from " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!";
	}

	public static String onUserUnbanFail(Channel c) {
		return ChatColor.RED + "You do not have permission to unban people in "
				+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!";
	}

	public static String onUserUnbannedAlready(String u, Channel c) {
		return ChatColor.YELLOW + u + ChatColor.RED +
				" is not banned in " + ChatColor.GOLD + c.getName()
				+ ChatColor.RED + "!";
	}

	public static String onUserMod(Channel c) {
		return ChatColor.GREEN + "You are now a mod in " + ChatColor.GOLD
				+ c.getName() + ChatColor.GREEN + "!";
	}

	public static String onUserModAnnounce(String user, Channel c) {
		return ChatColor.YELLOW + user + " is now a mod in " + 
				ChatColor.GOLD + c.getName() + ChatColor.YELLOW + "!";
	}

	public static String onUserModFail(Channel c) {
		return ChatColor.RED + "You do not have permission to mod people in "
				+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!";
	}

	public static String onUserModAlready(String user, Channel c) {
		return ChatColor.YELLOW + user + ChatColor.RED + " is already a mod in "
				+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!";
	}

	public static String onUserRmMod(SblockUser u, Channel c) {
		return null;
	}

	public static String onUserRmModAnnounce(SblockUser u, Channel c) {
		return null;
	}

	public static String isBanned(SblockUser u, Channel c) {
		return ChatColor.RED + "You are banned in channel " + ChatColor.GOLD
				+ c.getName() + ChatColor.RED + "!";
	}

	public static String onUserDeniedPrivateAccess(SblockUser u, Channel c) {
		return ChatColor.GOLD + c.getName() + ChatColor.RED
				+ " is a private channel!\n" + ChatColor.YELLOW
				+ "Request access with the command " + ChatColor.BLUE
				+ "/sc request " + c.getName();
	}

	public static String errorInvalidChannel(String c) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + c
				+ ChatColor.RED + " does not exist!";
	}

	public static String errorInvalidType(String s) {
		return ChatColor.RED + s + " is not a valid channel type!"
				+"\nValid types: Normal, CanonRP, RP, Nick, Temp.";
	}

	public static String unsupportedOperation(SblockUser u, Channel c) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + c.getName()
				+ ChatColor.RED + " does not support that operation.";
	}

	public static String errorInvalidAccess(String s) {
		return ChatColor.GOLD + s + ChatColor.RED +
				" is not a valid access level!\nValid levels: Public, Private";
	}

	public static String errorInvalidUser(String username) {
		return ChatColor.YELLOW + username + ChatColor.RED
				+ " does not exist! Get them to log in once.";
	}

	public static String errorAlreadyInChannel(String s) {
		return ChatColor.RED + "You are already listening to "
				+ ChatColor.GOLD + s;
	}

	public static String errorCannotLeaveCurrent(String s) {
		return ChatColor.RED + "Cannot leave your current channel "
				+ ChatColor.GOLD + s + ChatColor.RED + "!";
	}

	public static String errorNotListening(String s) {
		return ChatColor.RED + "You are not listening to channel "
				+ ChatColor.GOLD + s;
	}
}
