package com.easterlyn.chat.channel;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.ChannelManager;
import com.easterlyn.chat.Chat;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Defines default channel behavior
 *
 * @author Dublek, Jikoo
 */
public abstract class Channel {

	private final Easterlyn plugin;
	private final Users users;
	private final ChannelManager manager;
	private final Set<UUID> listening;
	/* Immutable Data regarding the channel */
	final String name;
	final UUID owner;

	/**
	 * @param name the name of the channel
	 * @param creator the owner of the channel
	 */
	Channel(Easterlyn plugin, String name, UUID creator) {
		this.plugin = plugin;
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		this.name = name;
		this.owner = creator;
		this.listening = Collections.newSetFromMap(new ConcurrentHashMap<>());
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
	public abstract boolean isOwner(User user);

	/**
	 * @param user a user
	 * @return whether this user has permission to moderate the channel
	 */
	public abstract boolean isModerator(User user);


	/**
	 * Check if the user is in the banlist AND not a denizen.
	 *
	 * @param user a user
	 * @return whether this user is banned
	 */
	public abstract boolean isApproved(User user);

	/**
	 * Check if the given OfflineUser is banned.
	 *
	 * @param user the OfflineUser
	 * @return true if the OfflineUser is banned
	 */
	public abstract boolean isBanned(User user);

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
	 * For sending a channel message, not for chat! Chat should be sent by constructing a Message with a MessageBuilder.
	 *
	 * @param message the message to send the channel.
	 */
	public void sendMessage(String message) {
		this.getListening().iterator().forEachRemaining(uuid -> this.getUsers().getUser(uuid).sendMessage(message));
		Logger.getLogger("Minecraft").info(message);
	}

	/**
	 * Gets the ChannelManager this Channel is registered in.
	 * 
	 * @return the ChannelManager
	 */
	public ChannelManager getChannelManager() {
		return this.manager;
	}

	/**
	 * Gets the Easterlyn instance creating this Channel.
	 * 
	 * @return the Easterlyn
	 */
	public Easterlyn getPlugin() {
		return this.plugin;
	}

	/**
	 * Gets the Users instance used to fetch Users.
	 * 
	 * @return the Users
	 */
	Users getUsers() {
		return users;
	}

	@Override
	public String toString() {
		return this.getName() + ": Access: " + this.getAccess() + " Type: "
				+ this.getClass().getSimpleName() + "\nOwner: "
				+ (this.owner != null ? Bukkit.getOfflinePlayer(this.getOwner()).getName() : "default");
	}

}
