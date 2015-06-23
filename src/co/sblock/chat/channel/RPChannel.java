package co.sblock.chat.channel;

import java.util.Map.Entry;
import java.util.UUID;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * Defines RP channel behavior
 * 
 * @author Dublek
 */
public class RPChannel extends NickChannel {
	
	/**
	 * @see co.sblock.chat.channel.Channel#Channel(String, AccessLevel, UUID)
	 */
	public RPChannel(String name, AccessLevel a, UUID creator, long lastAccessed) {
		super(name, a, creator, lastAccessed);
	}

	/**
	 * @see co.sblock.chat.channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(OfflineUser sender, String nick) {
		CanonNick name = CanonNick.getNick(nick);
		if (name == null) {
			return;
		}
		super.setNick(sender, name.getId());
	}

	/**
	 * Gets the OfflineUser using the nickname provided, or null if the nickname is not in use.
	 * 
	 * @param nick the nickname to reverse lookup
	 * @return the owner of the provided nickname
	 */
	@Override
	public OfflineUser getNickOwner(String nick) {
		CanonNick name = CanonNick.getNick(nick);
		if (name == null) {
			return null;
		}
		UUID remove = null;
		for (Entry<UUID, String> entry : nickList.entrySet()) {
			CanonNick canonNick = CanonNick.valueOf(entry.getValue());
			if (name == canonNick || name.getDisplayName().equals(canonNick.getDisplayName())) {
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
}
