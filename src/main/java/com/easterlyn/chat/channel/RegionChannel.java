package com.easterlyn.chat.channel;

import java.util.UUID;
import java.util.logging.Logger;

import com.easterlyn.Easterlyn;
import com.easterlyn.users.User;

public class RegionChannel extends Channel {

	/**
	 * Constructor for a RegionChannel. Regional channels all are hardcoded and may not be created
	 * or owned by a player.
	 */
	public RegionChannel(Easterlyn plugin, String name) {
		super(plugin, name, null);
	}

	/**
	 * Allows chat suppression for global channels.
	 * 
	 * @see com.easterlyn.chat.channel.Channel#sendMessage(User, String, boolean)
	 */
	@Override
	public void sendMessage(String message) {
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			User user = getUsers().getUser(userID);
			if (!user.getSuppression()) {
				user.sendMessage(message);
			}
		}
		Logger.getLogger("Minecraft").info(message);
	}

	/**
	 * Regional channels do not need last access updated; they cannot be deleted.
	 * 
	 * @see com.easterlyn.chat.channel.Channel#updateLastAccess()
	 */
	@Override
	public void updateLastAccess() {}

	/**
	 * Regional channels are never eligible for deletion.
	 * 
	 * @see com.easterlyn.chat.channel.Channel#isRecentlyAccessed()
	 */
	@Override
	public boolean isRecentlyAccessed() {
		return true;
	}

	/**
	 * Regional channels are public, but require moderator status to actually join.
	 * 
	 * @see com.easterlyn.chat.channel.Channel#getAccess()
	 */
	@Override
	public AccessLevel getAccess() {
		return AccessLevel.PUBLIC;
	}

	/**
	 * Owners and moderators are permission-based for regional channels.
	 * 
	 * @see com.easterlyn.chat.channel.Channel#isOwner(com.easterlyn.users.User)
	 */
	@Override
	public boolean isOwner(User user) {
		return user != null && user.getPlayer().hasPermission("sblock.chat.channel.owner");
	}

	/**
	 * Owners and moderators are permission-based for regional channels.
	 * 
	 * @see com.easterlyn.chat.channel.Channel#isModerator(com.easterlyn.users.User)
	 */
	@Override
	public boolean isModerator(User user) {
		return user != null && user.getPlayer().hasPermission("sblock.chat.channel.moderator");
	}

	/**
	 * All users are approved to focus on regional channels.
	 * 
	 * @see com.easterlyn.chat.channel.Channel#isApproved(com.easterlyn.users.User)
	 */
	@Override
	public boolean isApproved(User user) {
		return true;
	}

	/**
	 * Users cannot be banned from regional channels; they should be muted if they are that much of
	 * an issue.
	 * 
	 * @see com.easterlyn.chat.channel.Channel#isBanned(com.easterlyn.users.User)
	 */
	@Override
	public boolean isBanned(User user) {
		return false;
	}

}
