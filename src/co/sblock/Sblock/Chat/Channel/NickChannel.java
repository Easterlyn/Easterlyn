package co.sblock.Sblock.Chat.Channel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.ChatColor;

import co.sblock.Sblock.UserData.SblockUser;

public class NickChannel extends NormalChannel {
	
	protected HashMap<String, SblockUser> nickList;

	public NickChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
		// TODO Auto-generated constructor stub
	}
	
	public ChannelType getType() {
		return ChannelType.NICK;
	}
	public String getJoinChatMessage(SblockUser sender) {
		String time24h = new SimpleDateFormat("HH:mm").format(new Date());
		return ChatColor.DARK_GREEN + sender.getPlayerName() + ChatColor.YELLOW
				+ " began pestering " + ChatColor.GOLD + this.name
				+ ChatColor.YELLOW + " at " + time24h;
	}
	
	public void setNick(String nick, SblockUser sender) {
		this.nickList.put(nick, sender);
		sender.sendMessage(ChatColor.YELLOW + "Your nick has been set to \"" + ChatColor.BLUE + nick +
				"\" in " + ChatColor.GOLD + this.getName());
	}
	public void removeNick(SblockUser sender) {
		sender.sendMessage(ChatColor.RED
				+ "This channel does not support nicknames!");
	}

	public String getNickFromUser(SblockUser user)	{
		//TODO Goddammit Adam, it turns out we need your BiMaps after all. Freaking reverse lookups grumble grumble grumble
		return null;
	}
	public SblockUser getUserFromNick(String n)	{
		return nickList.get(n);
	}

}
