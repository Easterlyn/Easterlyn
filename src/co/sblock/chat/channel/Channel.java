package co.sblock.chat.channel;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import co.sblock.chat.Color;
import co.sblock.users.OfflineUser;

/**
 * Defines default channel behavior
 *
 * @author Dublek, Jikoo
 */
public abstract class Channel {

	/*
	 * Immutable Data regarding the channel
	 */
	protected final String name;
	protected final Set<UUID> listening;
	protected final UUID owner;

	private String lastMessage;

	/**
	 * @param name the name of the channel
	 * @param creator the owner of the channel
	 */
	public Channel(String name, UUID creator) {
		this.name = name;
		this.owner = creator;
		listening = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
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
	public abstract AccessLevel getAccess();

	/**
	 * @return all UUID's of users listening to this channel
	 */
	public Set<UUID> getListening() {
		return this.listening;
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
	public abstract boolean isOwner(OfflineUser user);

	/**
	 * @param user a user
	 * @return whether this user has permission to moderate the channel
	 */
	public abstract boolean isModerator(OfflineUser user);


	/**
	 * Check if the user is in the banlist AND not a denizen.
	 *
	 * @param user a user
	 * @return whether this user is banned
	 */
	public abstract boolean isApproved(OfflineUser user);

	/**
	 * Check if the given OfflineUser is banned.
	 *
	 * @param user the OfflineUser
	 * @return true if the OfflineUser is banned
	 */
	public abstract boolean isBanned(OfflineUser user);

	/**
	 * Check if the channel has been recently accessed and should not be deleted.
	 * 
	 * @return true if the channel should not be deleted
	 */
	public abstract boolean isRecentlyAccessed();

	/**
	 * Update the last access time.
	 */
	public abstract void updateLastAccess();

	/**
	 * Gets the last player chat message sent through the channel.
	 * 
	 * @return the message sent
	 */
	public String getLastMessage() {
		return lastMessage != null ? lastMessage : new String();
	}

	/**
	 * Sets the last player chat message sent through the channel.
	 * 
	 * @param message the message
	 */
	public void setLastMessage(String message) {
		lastMessage = message;
	}

	/**
	 * For sending a channel message, not for chat! Chat should be sent by constructing a Message with a MessageBuilder.
	 *
	 * @param message the message to send the channel.
	 */
	public abstract void sendMessage(String message);

	@Override
	public String toString() {
		return Color.GOOD_EMPHASIS + this.getName() + Color.GOOD + ": Access: " + Color.GOOD_EMPHASIS
				+ this.getAccess() + Color.GOOD + " Type: " + Color.GOOD_EMPHASIS + this.getClass().getSimpleName()
				+ "\n" + Color.GOOD + "Owner: " + Color.GOOD_EMPHASIS
				+ (this.owner != null ? Bukkit.getOfflinePlayer(this.getOwner()).getName() : "Sblock default");
	}
}
