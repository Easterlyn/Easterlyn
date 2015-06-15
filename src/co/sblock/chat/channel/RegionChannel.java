package co.sblock.chat.channel;

import java.util.UUID;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.Log;

public class RegionChannel extends Channel {

	/**
	 * Constructor for a RegionChannel. Regional channels all are hardcoded and may not be created
	 * or owned by a player.
	 */
	public RegionChannel(String name) {
		super(name, null);
	}

	/**
	 * Allows chat suppression for global channels.
	 * 
	 * @see co.sblock.chat.channel.Channel#sendMessage(User, String, boolean)
	 */
	@Override
	public void sendMessage(String message) {
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			OfflineUser u = Users.getGuaranteedUser(userID);
			if (u == null) {
				listening.remove(userID);
				continue;
			}
			if (!u.getSuppression()) {
				u.sendMessage(message);
			}
		}
		Log.anonymousInfo(message);
	}

	/**
	 * Regional channels do not need last access updated; they cannot be deleted.
	 * 
	 * @see co.sblock.chat.channel.Channel#updateLastAccess()
	 */
	@Override
	public void updateLastAccess() {}

	/**
	 * Regional channels are never eligible for deletion.
	 * 
	 * @see co.sblock.chat.channel.Channel#isRecentlyAccessed()
	 */
	@Override
	public boolean isRecentlyAccessed() {
		return true;
	}

	/**
	 * @see co.sblock.chat.channel.Channel#getAccess()
	 */
	@Override
	public AccessLevel getAccess() {
		return AccessLevel.PUBLIC;
	}

	/* (non-Javadoc)
	 * @see co.sblock.chat.channel.Channel#isOwner(co.sblock.users.OfflineUser)
	 */
	@Override
	public boolean isOwner(OfflineUser user) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see co.sblock.chat.channel.Channel#isModerator(co.sblock.users.OfflineUser)
	 */
	@Override
	public boolean isModerator(OfflineUser user) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see co.sblock.chat.channel.Channel#isApproved(co.sblock.users.OfflineUser)
	 */
	@Override
	public boolean isApproved(OfflineUser user) {
		return false;
	}

	/**
	 * @see co.sblock.chat.channel.Channel#isBanned(co.sblock.users.OfflineUser)
	 */
	@Override
	public boolean isBanned(OfflineUser user) {
		return false;
	}
}
