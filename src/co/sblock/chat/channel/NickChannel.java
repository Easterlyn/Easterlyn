package co.sblock.chat.channel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * Defines nick channel behavior
 * 
 * @author Dublek
 */
public class NickChannel extends Channel {

	protected transient Map<UUID, String> nickList; 

	/**
	 * @see co.sblock.chat.channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public NickChannel(String name, AccessLevel a, UUID creator) {
		super(name, a, creator);
		nickList = new ConcurrentHashMap<>();
	}

	/**
	 * @see co.sblock.chat.channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return ChannelType.NICK;
	}

	/**
	 * ONLY CALL FROM CHATUSER
	 *
	 * @param user the UUID to remove from listening.
	 */
	@Override
	public void removeListening(UUID userID) {
		this.listening.remove(userID);
		if (this.nickList.containsKey(userID)) {
			this.nickList.remove(userID);
		}
	}

	/**
	 * @see co.sblock.chat.channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(User sender, String nick) {
		nickList.put(sender.getUUID(), nick);
		this.sendMessage(ChatMsgs.onUserSetNick(sender.getPlayerName(), nick, this.name));
	}

	/**
	 * @see co.sblock.chat.channel.Channel#removeNick(ChatUser)
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
	 * @see co.sblock.chat.channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(User sender) {
		return nickList.containsKey(sender) ? nickList.get(sender) : sender.getPlayer().getDisplayName();
	}

	/**
	 * @see co.sblock.chat.channel.Channel#hasNick(ChatUser)
	 */
	@Override
	public boolean hasNick(User sender) {
		return nickList.containsKey(sender);
	}

	/**
	 * @see co.sblock.chat.channel.Channel#getNickOwner(String)
	 */
	@Override
	public User getNickOwner(String nick) {
		User owner = null;
		if (nickList.containsValue(nick)) {
			for (UUID u : nickList.keySet()) {
				if (nickList.get(u).equalsIgnoreCase(nick)) {
					owner = UserManager.getUser(u);
					break;
				}
			}
		}
		return owner;
	}

	/**
	 * Get the Map of nicks in use stored by UUID.
	 * 
	 * @return the Map
	 */
	public Map<UUID, String> getNickList() {
		return nickList;
	}

	@Override
	public ChannelSerialiser toSerialiser() {
		return new ChannelSerialiser(ChannelType.NICK, name, access, owner, approvedList, modList, muteList, banList, listening);
	}
}
