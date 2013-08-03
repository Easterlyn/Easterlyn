package co.sblock.Sblock.Chat.Channel;

import org.bukkit.ChatColor;

import com.google.common.collect.HashBiMap;

import co.sblock.Sblock.UserData.SblockUser;

public class NickChannel extends NormalChannel {
	
	protected HashBiMap<String, String> nickList;

	public NickChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
		nickList = HashBiMap.create();
	}

	public ChannelType getType() {
		return ChannelType.NICK;
	}

	public void setNick(SblockUser sender, String nick) {
		this.nickList.put(sender.getPlayerName(), nick);
		sender.sendMessage(ChatColor.YELLOW + "Your nick has been set to \""
				+ ChatColor.BLUE + nick + "\" in "
				+ ChatColor.GOLD + this.getName());
	}

	public void removeNick(SblockUser sender) {
		this.nickList.remove(sender.getPlayerName());
		sender.sendMessage(ChatColor.YELLOW + "Your nick has been reset to \""
				+ ChatColor.BLUE + sender.getNick() + "\" in " +
				ChatColor.GOLD + this.getName());
	}

	public CanonNicks getNick(SblockUser user) {
		return CanonNicks.CUSTOM.customize(getNickFromUser(user), null);
	}

	public String getNickFromUser(SblockUser user)	{
		return nickList.get(user.getPlayerName());
	}

	public String getUserFromNick(String n)	{
		return nickList.inverse().get(n);
	}
}
