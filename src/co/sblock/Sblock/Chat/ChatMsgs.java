package co.sblock.Sblock.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;

import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.UserData.SblockUser;

public class ChatMsgs {
	
	//I'm lazy, so I'm defining all strings sent directly to a User here.
	
	public static String onChannelJoin(SblockUser u, Channel c)	{
		String time24h = new SimpleDateFormat("HH:mm").format(new Date());
		return ChatColor.DARK_GREEN + u.getPlayerName() + ChatColor.YELLOW
				+ " began pestering " + ChatColor.GOLD + c.getName()
				+ ChatColor.YELLOW + " at " + time24h;		
	}
	public static String onChannelLeave(SblockUser u, Channel c)	{
		String time24h = new SimpleDateFormat("HH:mm").format(new Date());
		return ChatColor.DARK_GREEN + u.getPlayerName() + ChatColor.YELLOW
				+ " ceased pestering " + ChatColor.GOLD + c.getName()
				+ ChatColor.YELLOW + " at " + time24h;	
	}
	
	public static String onUserMute(SblockUser u)	{
		return ChatColor.RED + "You have been muted in all channels.";
	}
	public static String onUserUnmute(SblockUser u)	{
		return ChatColor.GREEN + "You have been unmuted in all channels.";
	}
	public static String isMute(SblockUser u)	{
		return ChatColor.RED + "You are muted in channel " + ChatColor.GOLD + u.getPlayerName() + ChatColor.RED + "!";
	}
	
	public static String onUserKick(SblockUser u, Channel c)	{
		return null;
	}
	public static String onUserBan(SblockUser u, Channel c)	{
		return null;
	}
	public static String onUserUnban(SblockUser u, Channel c)	{
		return null;
	}
	public static String isBanned(SblockUser u, Channel c)	{
		return ChatColor.RED + "You are banned in channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!";
	}
	
	public static String onUserDeniedPrivateAccess(SblockUser u, Channel c)	{
		return ChatColor.GOLD + c.getName() + ChatColor.RED + " is a private channel!\n" + 
				ChatColor.YELLOW + "Request access with the command " + ChatColor.BLUE + 
				"/sc request " + c.getName();
	}
	
	public static String errorInvalidChannel(Channel c)	{
		return ChatColor.RED + "Channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + " does not exist!";
	}
}
