package co.sblock.Sblock.Chat2.Channel;

import java.util.HashMap;
import java.util.Map;

import co.sblock.Sblock.Chat2.ChatMsgs;
import co.sblock.Sblock.Chat2.ChatUser;
import co.sblock.Sblock.Chat2.ChatUserManager;
/**
 * Defines nick channel behavior
 * 
 * @author Dublek
 */
public class NickChannel extends Channel {

	protected Map<ChatUser, String> nickList = new HashMap<ChatUser, String>();

	/**
	 * @param name
	 * @param a
	 * @param creator
	 */
	public NickChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}
	@Override
	public ChannelType getType() {
		return ChannelType.NICK;
	}
	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#setNick(co.sblock.Sblock.Chat2.ChatUser, java.lang.String)
	 */
	@Override
	public void setNick(ChatUser sender, String nick) {
		if (nickList.containsKey(sender))	{
			nickList.remove(sender);
		}
		nickList.put(sender, nick);
		for(String s : this.getListening())	{
			ChatUserManager.getUserManager().getUser(s).
			sendMessageFromChannel(ChatMsgs.onUserSetNick(s, nick, this), this, "channel");
		}
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#removeNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public void removeNick(ChatUser sender) {
		if (nickList.containsKey(sender))	{
			nickList.remove(sender);
		}		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#getNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public String getNick(ChatUser sender) {
		return nickList.get(sender);
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat2.Channel.Channel#hasNick(co.sblock.Sblock.Chat2.ChatUser)
	 */
	@Override
	public boolean hasNick(ChatUser sender) {
		return nickList.containsKey(sender);
	}
	@Override
	public ChatUser getNickOwner(String nick)	{
		ChatUser owner = null;
		if(nickList.containsValue(nick))	{
			for(ChatUser u : nickList.keySet())	{
				if(nickList.get(u).equalsIgnoreCase(nick))	{
					owner = u;
				}
			}
		}
		return owner;
	}
	
	public Map<ChatUser, String> getNickList()	{
		return nickList;
	}
}
