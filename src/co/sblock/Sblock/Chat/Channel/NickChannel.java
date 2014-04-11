package co.sblock.Sblock.Chat.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;

/**
 * Defines nick channel behavior
 * 
 * @author Dublek
 */
public class NickChannel extends Channel {

	protected Map<ChatUser, String> nickList; 

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public NickChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
		nickList = new HashMap<ChatUser, String>();

	}

	@Override
	public ChannelType getType() {
		return ChannelType.NICK;
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(ChatUser sender, String nick) {
		nickList.put(sender, nick);
		for (UUID userID : this.getListening()) {
			ChatUserManager.getUserManager().getUser(userID).sendMessageFromChannel(
					ChatMsgs.onUserSetNick(sender.getPlayerName(), nick, this.name), this, "channel");
		}
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#removeNick(ChatUser)
	 */
	@Override
	public void removeNick(ChatUser sender) {
		if (nickList.containsKey(sender)) {
			String old = nickList.remove(sender);
			for (UUID userID : this.getListening()) {
				ChatUserManager.getUserManager().getUser(userID).sendMessageFromChannel(
						ChatMsgs.onUserRmNick(sender.getPlayerName(), old, this.name), this, "channel");
			}
		}
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(ChatUser sender) {
		return nickList.containsKey(sender) ? nickList.get(sender) : sender.getGlobalNick();
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#hasNick(ChatUser)
	 */
	@Override
	public boolean hasNick(ChatUser sender) {
		return nickList.containsKey(sender);
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getNickOwner(String)
	 */
	@Override
	public ChatUser getNickOwner(String nick) {
		ChatUser owner = null;
		if (nickList.containsValue(nick)) {
			for (ChatUser u : nickList.keySet()) {
				if (nickList.get(u).equalsIgnoreCase(nick)) {
					owner = u;
				}
			}
		}
		return owner;
	}

	/**
	 * Get the Map of nicks in use stored by ChatUser.
	 * 
	 * @return the Map
	 */
	public Map<ChatUser, String> getNickList() {
		return nickList;
	}
}
