package co.sblock.chat.channel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import co.sblock.chat.ChatMsgs;
import co.sblock.users.OfflineUser;
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
	public void setNick(OfflineUser sender, String nick) {
		nickList.put(sender.getUUID(), nick);
		this.sendMessage(ChatMsgs.onUserSetNick(sender.getPlayerName(), nick, this.name));
	}

	/**
	 * @see co.sblock.chat.channel.Channel#removeNick(ChatUser)
	 */
	@Override
	public void removeNick(OfflineUser sender, boolean warn) {
		if (nickList.containsKey(sender.getUUID())) {
			String old = nickList.remove(sender.getUUID());
			if (warn) {
				this.sendMessage(ChatMsgs.onUserRmNick(sender.getPlayerName(), old, this.name));
			}
		}
	}

	/**
	 * @see co.sblock.chat.channel.Channel#getNick(ChatUser)
	 */
	@Override
	public String getNick(OfflineUser sender) {
		return nickList.containsKey(sender.getUUID()) ? nickList.get(sender.getUUID()) : sender.getPlayer().getDisplayName();
	}

	/**
	 * @see co.sblock.chat.channel.Channel#hasNick(ChatUser)
	 */
	@Override
	public boolean hasNick(OfflineUser sender) {
		return nickList.containsKey(sender.getUUID());
	}

	/**
	 * @see co.sblock.chat.channel.Channel#getNickOwner(String)
	 */
	@Override
	public OfflineUser getNickOwner(String nick) {
		OfflineUser owner = null;
		if (nickList.containsValue(nick)) {
			for (UUID u : nickList.keySet()) {
				if (nickList.get(u).equalsIgnoreCase(nick)) {
					owner = UserManager.getGuaranteedUser(u);
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
}
