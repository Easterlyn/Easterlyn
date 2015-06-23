package co.sblock.chat.channel;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * Defines nick channel behavior
 * 
 * @author Dublek
 */
public class NickChannel extends NormalChannel {

	protected transient Map<UUID, String> nickList; 

	/**
	 * @see co.sblock.chat.channel.NormalChannel#Channel(String, AccessLevel, UUID, Long)
	 */
	public NickChannel(String name, AccessLevel a, UUID creator, long lastAccessed) {
		super(name, a, creator, lastAccessed);
		nickList = new ConcurrentHashMap<>();
	}

	/**
	 * Sets a nickname for an OfflineUser.
	 * 
	 * @param user the OfflineUser
	 * @param nick the nickname
	 */
	public void setNick(OfflineUser user, String nick) {
		nickList.put(user.getUUID(), nick);
	}

	/**
	 * Removes a nickname from an OfflineUser.
	 * 
	 * @param user the OfflineUser
	 * @return the nick in use, or null if none.
	 */
	public String removeNick(OfflineUser user) {
		if (nickList.containsKey(user.getUUID())) {
			return nickList.remove(user.getUUID());
		}
		return null;
	}

	/**
	 * Gets the nickname of the given OfflineUser, defaulting to their name if they do not have one.
	 * 
	 * @param user the OfflinePlayer
	 * @return the nickname
	 */
	public String getNick(OfflineUser user) {
		return nickList.containsKey(user.getUUID()) ? nickList.get(user.getUUID())
				: user.isOnline() ? user.getPlayer().getDisplayName() : user.getPlayerName();
	}

	/**
	 * Check if the given OfflineUser has a nickname set in this Channel.
	 * 
	 * @param sender the OfflineUser
	 * @return true if a nickname is set
	 */
	public boolean hasNick(OfflineUser sender) {
		return nickList.containsKey(sender.getUUID());
	}

	/**
	 * Gets the OfflineUser using the nickname provided, or null if the nickname is not in use.
	 * 
	 * @param nick the nickname to reverse lookup
	 * @return the owner of the provided nickname
	 */
	public OfflineUser getNickOwner(String nick) {
		if (!nickList.containsValue(nick)) {
			return null;
		}
		UUID remove = null;
		for (Entry<UUID, String> entry : nickList.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(nick)) {
				if (!getListening().contains(entry.getKey())) {
					remove = entry.getKey();
					break;
				}
				return Users.getGuaranteedUser(entry.getKey());
			}
		}
		if (remove != null) {
			nickList.remove(remove);
		}
		return null;
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
