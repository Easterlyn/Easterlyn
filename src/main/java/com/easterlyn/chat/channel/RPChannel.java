package com.easterlyn.chat.channel;

import java.util.Map.Entry;

import com.easterlyn.Easterlyn;
import com.easterlyn.users.User;

import java.util.UUID;

/**
 * Defines RP channel behavior
 * 
 * @author Dublek
 */
public class RPChannel extends NickChannel {

	public RPChannel(Easterlyn plugin, String name, AccessLevel a, UUID creator, long lastAccessed) {
		super(plugin, name, a, creator, lastAccessed);
	}

	/**
	 * @see com.easterlyn.chat.channel.Channel#setNick(ChatUser, String)
	 */
	@Override
	public void setNick(User sender, String nick) {
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
	public User getNickOwner(String nick) {
		CanonNick name = CanonNick.getNick(nick);
		if (name == null) {
			return null;
		}
		UUID remove = null;
		for (Entry<UUID, String> entry : nickList.entrySet()) {
			CanonNick canonNick = CanonNick.getNick(entry.getValue());
			if (name == canonNick || name.getDisplayName().equals(canonNick.getDisplayName())) {
				if (!getListening().contains(entry.getKey())) {
					remove = entry.getKey();
					break;
				}
				return getUsers().getUser(entry.getKey());
			}
		}
		if (remove != null) {
			nickList.remove(remove);
		}
		return null;
	}

}
