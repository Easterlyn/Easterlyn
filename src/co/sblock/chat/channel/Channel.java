package co.sblock.chat.channel;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Chat;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.Log;
import co.sblock.utilities.threadsafe.SetGenerator;

/**
 * Defines default channel behavior
 *
 * @author Dublek, Jikoo
 */
public abstract class Channel {

	/*
	 * Immutable Data regarding the channel
	 */
	protected String name;
	protected AccessLevel access;
	protected Set<UUID> approvedList;
	protected Set<UUID> modList;
	protected Set<UUID> muteList;
	protected Set<UUID> banList;
	protected Set<UUID> listening;
	protected UUID owner;

	/**
	 * @param name the name of the channel
	 * @param a the access level of the channel
	 * @param creator the owner of the channel
	 */
	public Channel(String name, AccessLevel a, UUID creator) {
		this.name = name;
		this.access = a;
		this.owner = creator;
		approvedList = SetGenerator.generate();
		modList = SetGenerator.generate();
		muteList = SetGenerator.generate();
		banList = SetGenerator.generate();
		listening = SetGenerator.generate();
		if (creator != null) {
			modList.add(creator);
			ChannelManager.getChannelManager().saveChannel(this);
		}
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
	public AccessLevel getAccess() {
		return this.access;
	}

	/**
	 * @return all UUID's of users listening to this channel
	 */
	public Set<UUID> getListening() {
		return this.listening;
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
	 * @return the UUID of the channel owner
	 */
	public UUID getOwner() {
		return this.owner;
	}




	/* TESTERS */
	/**
	 * @param user a user
	 * @return if this user is an owner (created channel / set by previous owner, or is a denizen)
	 */
	public boolean isOwner(OfflineUser user) {
		return user != null && (user.getUUID().equals(owner)
				|| user.isOnline() && user.getOnlineUser().getPlayer().hasPermission("group.denizen"));
	}

	/**
	 * @param user a user
	 * @return whether this user has permission to moderate the channel
	 */
	public boolean isModerator(OfflineUser user) {
		return user != null && (isOwner(user) || modList.contains(user.getUUID())
				|| user.isOnline() && user.getOnlineUser().getPlayer().hasPermission("group.felt"));
	}

	/**
	 * the user must be in the banlist AND not a denizen
	 *
	 * @param user a user
	 * @return whether this user is banned
	 */
	public boolean isBanned(OfflineUser user) {
		return banList.contains(user.getUUID()) && !isOwner(user);
	}





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
	 * Change the owner of a channel. This should only be useable by admins/the current owner.
	 *
	 * @param newOwner the new owner
	 */
	public void setOwner(UUID newOwner) {
		this.owner = newOwner;
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
	public void addMod(OfflineUser sender, UUID userID) {
		if (!isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		OfflineUser user = Users.getGuaranteedUser(userID);
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
	public void removeMod(OfflineUser sender, UUID userID) {
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		OfflineUser user = Users.getGuaranteedUser(userID);
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
	 * @return the type of this channel
	 */
	public abstract ChannelType getType();

	/**
	 * 
	 *
	 * @param sender the user attempting to kick
	 * @param userID the user who might be kicked
	 */
	public void kickUser(OfflineUser sender, UUID userID) {
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		OfflineUser user = Users.getGuaranteedUser(userID);
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

	public void banUser(OfflineUser sender, UUID userID) {
		if (!this.isModerator(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		OfflineUser user = Users.getGuaranteedUser(userID);
		if (user == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		String message = ChatMsgs.onUserBanAnnounce(Bukkit.getPlayer(userID).getName(), this.name);
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

	public void unbanUser(OfflineUser sender, UUID userID) {
		if (!this.isOwner(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		OfflineUser user = Users.getGuaranteedUser(userID);
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

	public void approveUser(OfflineUser sender, UUID target) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
			return;
		} else {
			OfflineUser targ = Users.getGuaranteedUser(target);
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

	public void disapproveUser(OfflineUser sender, UUID target) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name));
			return;
		} else {
			OfflineUser targ = Users.getGuaranteedUser(target);
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

	public boolean isApproved(OfflineUser user) {
		return access == AccessLevel.PUBLIC || approvedList.contains(user.getUUID()) || isModerator(user);
	}

	public void disband(OfflineUser sender) {
		if (this.owner == null) {
			sender.sendMessage(ChatMsgs.errorDisbandDefault());
			return;
		}
		if (!this.isOwner(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name));
			return;
		}
		this.sendMessage(ChatMsgs.onChannelDisband(this.getName()));
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			Users.getGuaranteedUser(userID).removeListeningSilent(this);
		}
		Chat.getChat().getChannelManager().dropChannel(this.name);
	}

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

	public String toString() {
		return ChatColor.GOLD + this.getName() + ChatColor.GREEN + ": Access: " + ChatColor.GOLD
				+ this.getAccess() + ChatColor.GREEN + " Type: " + ChatColor.GOLD + this.getType()
				+ "\n" + ChatColor.GREEN + "Owner: " + ChatColor.GOLD
				+ (this.owner != null ? Bukkit.getOfflinePlayer(this.getOwner()).getName() : "Sblock default");
	}
}
