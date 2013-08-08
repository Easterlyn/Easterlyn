package co.sblock.Sblock.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;

import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.UserData.SblockUser;

public class ChatMsgs {

	public static String onChannelJoin(SblockUser u, Channel c) {
		return c.getNick(u).getName() + ChatColor.YELLOW + " began "
				+ c.getNick(u).getPester() + " " + ChatColor.GOLD
				+ c.getName() + ChatColor.YELLOW + " at "
				+ new SimpleDateFormat("HH:mm").format(new Date());
	}

	public static String onChannelLeave(SblockUser u, Channel c) {
		return ChatMsgs.onChannelJoin(u, c).replaceAll("began", "ceased");
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

	public static String onUserKick(SblockUser u, Channel c) {

		return null;
	}

	public static String onUserKickAnnounce(SblockUser u, Channel c) {

		return null;
	}

	public static String onUserBan(SblockUser u, Channel c) {
		return null;
	}

	public static String onUserBanAnnounce(SblockUser u, Channel c) {
		return null;
	}

	public static String onUserUnban(SblockUser u, Channel c) {
		return null;
	}

	public static String onUserUnbanAnnounce(SblockUser u, Channel c) {
		return null;
	}

	public static String onUserMod(SblockUser u, Channel c) {
		return null;
	}

	public static String onUserModAnnounce(SblockUser u, Channel c) {
		return null;
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

	public static String errorInvalidChannel(Channel c) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + c.getName()
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
}
