package co.sblock.chat.channel;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import co.sblock.Sblock;
import co.sblock.chat.ChatMsgs;
import co.sblock.users.User;

/**
 * Defines normal channel behavior.
 * 
 * @author Dublek, tmathmeyer
 */
public class NormalChannel extends Channel {

	protected final AccessLevel access;
	protected final Set<UUID> approvedList;
	protected final Set<UUID> modList;
	protected final Set<UUID> banList;
	private final AtomicLong lastAccessed;

	public NormalChannel(Sblock plugin, String name, AccessLevel access, UUID creator, long lastAccessed) {
		super(plugin, name, creator);
		this.access = access;
		approvedList = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
		modList = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
		banList = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
		this.lastAccessed = new AtomicLong(lastAccessed);
		if (creator != null) {
			modList.add(creator);
		}
	}

	@Override
	public AccessLevel getAccess() {
		return access;
	}

	/**
	 * @return all UUID's of the mods of this channel
	 */
	public Set<UUID> getModList() {
		return this.modList;
	}

	/**
	 * @return all UUID's of the players banned from this channel
	 */
	public Set<UUID> getBanList() {
		return banList;
	}

	/**
	 * @param user a user
	 * @return if this user is an owner (created channel / set by previous owner, or is a denizen)
	 */
	@Override
	public boolean isOwner(User user) {
		return user != null && (user.getUUID().equals(owner) || user.getPlayer().hasPermission("sblock.denizen"));
	}

	/**
	 * @param user a user
	 * @return whether this user has permission to moderate the channel
	 */
	@Override
	public boolean isModerator(User user) {
		return user != null && (isOwner(user) || modList.contains(user.getUUID())
				|| user.getPlayer().hasPermission("sblock.felt"));
	}

	/**
	 * Check if the user is in the banlist AND not a denizen.
	 *
	 * @param user a user
	 * @return whether this user is banned
	 */
	@Override
	public boolean isBanned(User user) {
		return banList.contains(user.getUUID()) && !isOwner(user);
	}

	/**
	 * @param userID the user to add to the approval list
	 */
	public void addApproved(UUID userID) {
		this.approvedList.add(userID);
	}

	/**
	 * Method used by database to load a ban silently.
	 *
	 * @param user the UUID to add as a ban
	 */
	public void addBan(UUID userID) {
		this.banList.add(userID);
	}

	/**
	 * Method used by database to load a moderator silently.
	 *
	 * @param user the name to add as a moderator
	 */
	public void addModerator(UUID id) {
		modList.add(id);
	}

	/**
	 * @param sender the person attempting to apply moderator status to another
	 * @param userID the ID of the person who may become a mod
	 */
	public void addMod(User sender, UUID userID) {
		if (!isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		User user = getUsers().getUser(userID);
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		String message = ChatMsgs.onChannelModAdd(user.getDisplayName(), this.name);
		if (!this.isModerator(user)) {
			this.modList.add(userID);
			this.sendMessage(message);
			if (!this.listening.contains(userID)) {
				user.sendMessage(message);
			}
		} else {
			sender.sendMessage(message);
		}
	}

	/**
	 * @param sender the person attempting to remove moderator status from another
	 * @param userID the ID of the person who may lose mod status
	 */
	public void removeMod(User sender, UUID userID) {
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		User user = getUsers().getUser(userID);
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		String message = ChatMsgs.onChannelModRm(user.getDisplayName(), this.name);
		if (this.modList.contains(userID) && !this.isOwner(user)) {
			this.modList.remove(userID);
			this.sendMessage(message);
			if (!this.listening.contains(userID)) {
				user.sendMessage(message);
			}
		} else {
			sender.sendMessage(message);
		}
	}

	/**
	 * @param sender the user attempting to kick
	 * @param userID the user who might be kicked
	 */
	public void kickUser(User sender, UUID userID) {
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		User user = getUsers().getUser(userID);
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		String message = ChatMsgs.onUserKickAnnounce(user.getPlayerName(), this.name);
		if (this.isOwner(user)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
		} else if (listening.contains(user.getPlayerName())) {
			this.sendMessage(message);
			this.listening.remove(user.getUUID());
			user.removeListening(this.getName());
		} else {
			sender.sendMessage(message);
		}
	}

	public void banUser(User sender, UUID userID) {
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		User user = getUsers().getUser(userID);
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		String message = ChatMsgs.onUserBanAnnounce(user.getPlayerName(), this.name);
		if (this.isOwner(user)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
		} else if (!this.isBanned(user)) {
			if (modList.contains(userID)) {
				modList.remove(userID);
			}
			this.approvedList.remove(userID);
			this.banList.add(userID);
			this.sendMessage(message);
			if (listening.contains(userID)) {
				user.removeListening(this.getName());
			}
		} else {
			sender.sendMessage(message);
		}
	}

	public void unbanUser(User sender, UUID userID) {
		if (!this.isOwner(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		User user = getUsers().getUser(userID);
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		String message = ChatMsgs.onUserUnbanAnnounce(user.getPlayerName(), this.name);
		if (banList.contains(userID)) {
			this.banList.remove(userID);
			user.sendMessage(message);
			this.sendMessage(message);
		} else {
			sender.sendMessage(message);
		}
	}

	public void approveUser(User sender, UUID target) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
			return;
		} else {
			User targ = getUsers().getUser(target);
			String message = ChatMsgs.onUserApproved(targ.getPlayerName(), this.name);
			if (this.isApproved(targ)) {
				sender.sendMessage(message);
				return;
			}
			approvedList.add(target);
			this.sendMessage(message);
			targ.sendMessage(message);
		}
	}

	public void disapproveUser(User sender, UUID target) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
			return;
		} else {
			User targ = getUsers().getUser(target);
			String message = ChatMsgs.onUserDeapproved(targ.getPlayerName(), this.name);
			if (!this.isApproved(targ)) {
				sender.sendMessage(message);
				return;
			}
			approvedList.remove(target);
			this.sendMessage(message);
			targ.removeListeningSilent(this);
		}
	}

	public Set<UUID> getApprovedUsers() {
		return approvedList;
	}

	@Override
	public boolean isApproved(User user) {
		return access == AccessLevel.PUBLIC || approvedList.contains(user.getUUID()) || isModerator(user);
	}

	@Override
	public boolean isRecentlyAccessed() {
		// 1000 ms/s * 60 s/min * 60 min/hr * 24 hr/d * 30d
		return lastAccessed.get() + 2592000000L > System.currentTimeMillis();
	}

	@Override
	public void updateLastAccess() {
		this.lastAccessed.set(System.currentTimeMillis());
	}

	/**
	 * Gets the last access time of this Channel.
	 * 
	 * @return the last access time.
	 */
	public long getLastAccess() {
		return this.lastAccessed.get();
	}

	public void disband(User sender) {
		if (this.owner == null) {
			sender.sendMessage(ChatMsgs.errorDisbandDefault());
			return;
		}
		if (sender != null && !this.isOwner(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		this.sendMessage(ChatMsgs.onChannelDisband(this.getName()));
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			getUsers().getUser(userID).removeListeningSilent(this);
		}
		getChannelManager().dropChannel(this.name);
	}

	@Override
	public void sendMessage(String message) {
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			User u = getUsers().getUser(userID);
			if (u == null) {
				listening.remove(userID);
				continue;
			}
			u.sendMessage(message);
		}
		Logger.getLogger("Minecraft").info(message);
	}
}
