package co.sblock.Sblock.Chat.Channel;

import java.util.HashMap;
import java.util.Map;

import co.sblock.Sblock.Chat2.ChatMsgs;
import co.sblock.Sblock.Chat2.ChatUser;
import co.sblock.Sblock.Chat2.ChatUserManager;
import co.sblock.Sblock.Chat2.Channel.Channel;
import co.sblock.Sblock.Chat2.Channel.AccessLevel;
import co.sblock.Sblock.Chat2.Channel.ChannelType;

public class NickChannel extends Channel {

	private Map<ChatUser, String> nickList = new HashMap<ChatUser, String>();
	
	public NickChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}
	
	public ChannelType getType()	{
		return ChannelType.NICK;
	}
	
	public void setNick(ChatUser sender, String nick) {
		nickList.put(sender, nick);
		for(String user : this.getListening()){
			ChatUserManager.getUserManager().getUser(user).sendMessage(ChatMsgs.onUserSetNick(sender.getPlayerName(), nick, this));
		}
	}

	public void removeNick(ChatUser sender) {
		for(String user : this.getListening()){
			ChatUserManager.getUserManager().getUser(user).sendMessage(
					ChatMsgs.onUserRmNick(sender.getPlayerName(), nickList.get(sender), this));
		}
		nickList.remove(sender);
	}
	
	public String getNick(ChatUser sender) {
		return nickList.get(sender);
	}
	
	@Override
	public boolean hasNick(ChatUser sender)	{
		return nickList.containsKey(sender);
	}
/*	protected HashBiMap<String, String> nickList;

	public NickChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
		nickList = HashBiMap.create();
	}

	public ChannelType getType() {
		return ChannelType.NICK;
	}

	public void setNick(ChatUser sender, String nick) {
		if (getUserFromNick(nick) != null) {
			sender.sendMessage(ChatColor.RED + "The nick " + ChatColor.BLUE
					+ nick + ChatColor.RED + " is already in use!");
		}
		this.nickList.put(sender.getPlayerName(), nick);
		sender.sendMessage(ChatColor.YELLOW + "Your nick has been set to \""
				+ ChatColor.BLUE + nick + "\" in "
				+ ChatColor.GOLD + this.getName());
	}

	public void removeNick(ChatUser sender) {
		this.nickList.remove(sender.getPlayerName());
		sender.sendMessage(ChatColor.YELLOW + "Your nick has been reset to \""
				+ ChatColor.BLUE + sender.getNick() + "\" in " +
				ChatColor.GOLD + this.getName());
	}

	@Override
	public Nick getNick(ChatUser user) {
		return this.nickList.get(user.getPlayerName()) != null ?
				new Nick(this.nickList.get(user.getPlayerName())) : new Nick(user.getNick());
	}

	public String getUserFromNick(String n)	{
		return nickList.inverse().get(n);
	}*/
}
