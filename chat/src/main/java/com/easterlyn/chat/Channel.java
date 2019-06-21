package com.easterlyn.chat;

import com.easterlyn.users.User;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Channel {

	private final String name;
	private final UUID owner;
	private final Set<UUID> listeners;

	public Channel(String name, UUID owner) {
		this.name = name;
		this.owner = owner;
		this.listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	/**
	 * @return the channel's name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return this channel's access level
	 */
	public AccessLevel getAccess() {
		return AccessLevel.PUBLIC;
	}

	/**
	 * @return all UUID's of users listening to this channel
	 */
	public Set<UUID> getListening() {
		return this.listeners;
	}

	/**
	 * @return the UUID of the channel owner
	 */
	public UUID getOwner() {
		return this.owner;
	}

	/**
	 * @param user a user
	 * @return if this user is an owner
	 */
	public boolean isOwner(User user) {
		return user.getUniqueId().equals(getOwner()) || user.hasPermission("easterlyn.chat.channel.owner");
	}

	/**
	 * @param user a user
	 * @return whether this user has permission to moderate the channel
	 */
	public boolean isModerator(User user) {
		return user.hasPermission("easterlyn.chat.channel.moderator");
	}


	/**
	 * Check if the user is in the banlist AND not a denizen.
	 *
	 * @param user a user
	 * @return whether this user is banned
	 */
	public boolean isApproved(User user) {
		return true;
	}

	/**
	 * Check if the given OfflineUser is banned.
	 *
	 * @param user the OfflineUser
	 * @return true if the OfflineUser is banned
	 */
	public boolean isBanned(User user) {
		return false;
	}

	/**
	 * Check if the channel has been recently accessed and should not be deleted.
	 *
	 * @return true if the channel should not be deleted
	 */
	public boolean isRecentlyAccessed() {
		return true;
	}

	/**
	 * Update the last access time.
	 */
	public void updateLastAccess() {}

}
