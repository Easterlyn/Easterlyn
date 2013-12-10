package co.sblock.Sblock.Chat.Channel;

import java.util.HashMap;
import java.util.Map;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;

/**
 * Defines nick channel behavior
 * 
 * @author Dublek
 */
public class NickChannel extends Channel {

	protected Map<ChatUser, String> nickList = new HashMap<ChatUser, String>();

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#Channel(String, AccessLevel,
	 *      String)
	 */
	public NickChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
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
		for (String s : this.getListening()) {
			ChatUserManager.getUserManager().getUser(s).sendMessageFromChannel(
					ChatMsgs.onUserSetNick(s, nick, this.name), this, "channel");
		}
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#removeNick(ChatUser)
	 */
	@Override
	public void removeNick(ChatUser sender) {
		if (nickList.containsKey(sender)) {
			nickList.remove(sender);
		}
	}

	/**
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(ChatUser sender) {
		return nickList.get(sender);
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
	 * Get the <code>Map</code> of nicks in use stored by <code>ChatUser</code>.
	 * 
	 * @return <code>Map</code>
	 */
	public Map<ChatUser, String> getNickList() {
		return nickList;
	}
}
