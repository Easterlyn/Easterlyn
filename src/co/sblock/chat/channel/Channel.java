package co.sblock.chat.channel;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import co.sblock.chat.Color;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.Log;

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

	/**
	 * @param name the name of the channel
	 * @param a the access level of the channel
	 * @param creator the owner of the channel
	 */
	public Channel(String name, UUID creator, long lastAccessed) {
		this.name = name;
		this.owner = creator;
		listening = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
	}

	/* GETTERS */
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
	 * @return the type of this channel
	 */
	public abstract ChannelType getType();

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




	/* TESTERS */
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





	/* ADDERS / REMOVERS */
	/**
	 * TODO: enforce this
	 * ONLY CALL FROM USER
	 *
	 * @param userID the user UUID to add listening
	 */
	public void addListening(UUID userID) {
		this.listening.add(userID);
	}

	/**
	 * ONLY CALL FROM CHATUSER
	 *
	 * @param user the UUID to remove from listening.
	 */
	public void removeListening(UUID userID) {
		this.listening.remove(userID);
	}

	/**
	 *
	 * @param sender the sender
	 * @param nick the nick
	 */
	public abstract void setNick(OfflineUser sender, String nick);

	/**
	 *
	 * @param sender the sender
	 * @param warn whether to warn the user
	 */
	public abstract void removeNick(OfflineUser sender, boolean warn);

	/**
	 *
	 * @param sender the sender
	 * @return the nick of the sender
	 */
	public abstract String getNick(OfflineUser sender);

	/**
	 *
	 * @param sender the sender
	 * @return whether the sender has had a nick set
	 */
	public abstract boolean hasNick(OfflineUser sender);

	/**
	 *
	 * @param nick the nickname to reverse lookup
	 * @return the owner of the provided nickname
	 */
	public abstract OfflineUser getNickOwner(String nick);

	/**
	 * For sending a channel message, not for chat! Chat should be handled by getting a Message from
	 * ChannelManager.
	 *
	 * @param message the message to send the channel.
	 */
	public void sendMessage(String message) {
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			OfflineUser u = Users.getGuaranteedUser(userID);
			if (u == null) {
				listening.remove(userID);
				continue;
			}
			u.sendMessage(message);
		}
		Log.anonymousInfo(message);
	}

	@Override
	public String toString() {
		return Color.GOOD_EMPHASIS + this.getName() + Color.GOOD + ": Access: " + Color.GOOD_EMPHASIS
				+ this.getAccess() + Color.GOOD + " Type: " + Color.GOOD_EMPHASIS + this.getType()
				+ "\n" + Color.GOOD + "Owner: " + Color.GOOD_EMPHASIS
				+ (this.owner != null ? Bukkit.getOfflinePlayer(this.getOwner()).getName() : "Sblock default");
	}
}
