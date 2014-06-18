package co.sblock.chat.channel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.User;

/**
 * Defines nick channel behavior
 * 
 * @author Dublek
 */
public class NickChannel extends Channel {

	protected Map<User, String> nickList;

	/**
	 * @see co.sblock.Chat.Channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public NickChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
		nickList = new ConcurrentHashMap<>();
	}

	@Override
	public ChannelType getType() {
		return ChannelType.NICK;
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(User sender, String nick) {
		nickList.put(sender, nick);
		this.sendMessage(ChatMsgs.onUserSetNick(sender.getPlayerName(), nick, this.name));
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#removeNick(ChatUser)
	 */
	@Override
	public void removeNick(User sender, boolean warn) {
		if (nickList.containsKey(sender)) {
			String old = nickList.remove(sender);
			if (warn) {
				this.sendMessage(ChatMsgs.onUserRmNick(sender.getPlayerName(), old, this.name));
			}
		}
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(User sender) {
		return nickList.containsKey(sender) ? nickList.get(sender) : sender.getPlayer()
				.getDisplayName();
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#hasNick(ChatUser)
	 */
	@Override
	public boolean hasNick(User sender) {
		return nickList.containsKey(sender);
	}

	/**
	 * @see co.sblock.Chat.Channel.Channel#getNickOwner(String)
	 */
	@Override
	public User getNickOwner(String nick) {
		User owner = null;
		if (nickList.containsValue(nick)) {
			for (User u : nickList.keySet()) {
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
	public Map<User, String> getNickList() {
		return nickList;
	}
}
