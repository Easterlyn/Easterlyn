package co.sblock.chat.channel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import co.sblock.chat.ChatMsgs;
import co.sblock.chat.ColorDef;
import co.sblock.chat.SblockChat;
import co.sblock.data.SblockData;
import co.sblock.users.ChatData;
import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.utilities.Log;


/**
 * Defines default channel behavior
 * 
 * @author Dublek, Jikoo
 */
public abstract class Channel {

	protected String name;
	protected AccessLevel access;
	protected UUID owner;
	protected Set<UUID> approvedList;
	protected Set<UUID> modList;
	protected Set<UUID> muteList;
	protected Set<UUID> banList;
	protected Set<UUID> listening;
	
	
	public Channel(String name, AccessLevel a, UUID creator) {
		this.name = name;
		this.access = a;
		this.owner = creator;
		approvedList = new HashSet<>();
		modList = new HashSet<>();
		if (creator != null) {
			this.modList.add(creator);
		}
		muteList = new HashSet<>();
		banList = new HashSet<>();
		listening = new HashSet<>();
		if (creator != null) {
			SblockData.getDB().saveChannelData(this);
		}
	}

	public String getName() {
		return this.name;
	}

	public AccessLevel getAccess() {
		return this.access;
	}

	public Set<UUID> getListening() {
		return this.listening;
	}

	public abstract ChannelType getType();
	
	/**
	 * ONLY CALL FROM CHATUSER
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

	public abstract void setNick(User sender, String nick);

	public abstract void removeNick(User sender);

	public abstract String getNick(User sender);

	public abstract boolean hasNick(User sender);

	public abstract User getNickOwner(String nick);
	
	public void setOwner(User sender, UUID newOwner) {
		if (sender.equals(this.owner)) {
			this.owner = newOwner;
		}
	}

	public UUID getOwner() {
		return this.owner;
	}

	public boolean isOwner(User sender) {
		return sender.getUUID().equals(owner) || sender.getPlayer().hasPermission("group.denizen");
	}

	/**
	 * Method used by database to load a moderator silently.
	 * 
	 * @param user the name to add as a moderator
	 */
	public void loadMod(UUID id) {
		this.modList.add(id);
	}

	public void addMod(User sender, UUID userID) {
		if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name), false);
			return;
		}
		if (User.getUser(userID) == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()), false);
			return;
		}
		User user = User.getUser(userID);
		String message = ChatMsgs.onChannelModAdd(user.getPlayerName(), this.name);
		if (!this.isChannelMod(User.getUser(userID))) {
			this.modList.add(userID);
			this.sendToAll(sender, message, false);
			if (!this.listening.contains(userID)) {
				user.sendMessage(message, true);
			}
		} else {
			sender.sendMessage(message, false);
		}
	}

	public void removeMod(User sender, UUID userID) {
		if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name), false);
			return;
		}
		if (User.getUser(userID) == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()), false);
			return;
		}
		User user = User.getUser(userID);
		String message = ChatMsgs.onChannelModRm(user.getPlayerName(), this.name);
		if (this.modList.contains(userID) && !this.isOwner(user)) {
			this.modList.remove(userID);
			this.sendToAll(sender, message, false);
			if (!this.listening.contains(userID)) {
				user.sendMessage(message, true);
			}
		} else {
			sender.sendMessage(message, false);
		}
	}

	public Set<UUID> getModList() {
		return this.modList;
	}

	public boolean isChannelMod(User sender) {
		return isMod(sender) || sender.getPlayer().hasPermission("group.felt");
	}

	public boolean isMod(User sender) {
		return modList.contains(sender.getUUID()) || sender.getPlayer().hasPermission("group.denizen");
	}

	public void kickUser(User sender, UUID userID) {
		if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name), false);
			return;
		}
		if (User.getUser(userID) == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()), false);
			return;
		}
		User user = User.getUser(userID);
		String message = ChatMsgs.onUserKickAnnounce(user.getPlayerName(), this.name);
		if (this.isOwner(user)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name), false);
		} else if (listening.contains(user.getPlayerName())) {
			this.sendToAll(sender, message, false);
			this.listening.remove(user.getUUID());
			ChatData.removeListening(user, this.getName());
		} else {
			sender.sendMessage(message, false);
		}

	}
	
	/**
	 * Method used by database to load a ban silently.
	 * 
	 * @param user the UUID to add as a ban
	 */
	public void loadBan(UUID userID) {
		this.banList.add(userID);
	}

	public void banUser(User sender, UUID userID) {
		if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name), false);
			return;
		}
		if (User.getUser(userID) == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()), false);
			return;
		}
		User user = User.getUser(userID);
		String message = ChatMsgs.onUserBanAnnounce(Bukkit.getPlayer(userID).getName(), this.name);
		if (this.isOwner(user)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name), false);
		} else if (!this.isBanned(user)) {
			if (modList.contains(userID)) {
				modList.remove(userID);
			}
			this.approvedList.remove(userID);
			this.banList.add(userID);
			this.sendToAll(sender, message, false);
			if (listening.contains(userID)) {
				ChatData.removeListening(user, this.getName());
			}
		} else {
			sender.sendMessage(message, false);
		}
	}

	public void unbanUser(User sender, UUID userID) {
		if (!this.isOwner(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name), false);
			return;
		}
		if (User.getUser(userID) == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()), false);
			return;
		}
		User user = User.getUser(userID);
		String message = ChatMsgs.onUserUnbanAnnounce(user.getPlayerName(), this.name);
		if (banList.contains(userID)) {
			this.banList.remove(userID);
			user.sendMessage(message, true);
			this.sendToAll(sender, message, false);
		} else {
			sender.sendMessage(message, false);
		}
	}

	public Set<UUID> getBanList() {
		return banList;
	}

	public boolean isBanned(User user) {
		return banList.contains(user.getUUID());
	}

	public void loadApproval(UUID userID) {
		this.approvedList.add(userID);
	}

	public void approveUser(User sender, UUID target) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name), false);
			return;
		} else {
			User targ = User.getUser(target);
			String message = ChatMsgs.onUserApproved(targ.getPlayerName(), this.name);
			if (this.isApproved(targ)) {
				sender.sendMessage(message, false);
				return;
			}
			approvedList.add(target);
			this.sendToAll(sender, message, false);
			targ.sendMessage(message, true);
		}
	}

	public void deapproveUser(User sender, UUID target) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatMsgs.unsupportedOperation(this.name), false);
			return;
		} else {
			User targ = User.getUser(target);
			String message = ChatMsgs.onUserDeapproved(targ.getPlayerName(), this.name);
			if (!this.isApproved(targ)) {
				sender.sendMessage(message, false);
				return;
			}
			approvedList.remove(target);
			this.sendToAll(sender, message, false);
			ChatData.removeListeningSilent(targ, this);
		}
	}

	public Set<UUID> getApprovedUsers() {
		return approvedList;
	}

	public boolean isApproved(User user) {
		return approvedList.contains(user.getUUID()) || isChannelMod(user);
	}

	public void disband(User sender) {
		if (this.owner == null) {
			sender.sendMessage(ChatMsgs.errorDisbandDefault(), false);
			return;
		}
		if (!this.isOwner(sender)) {
			sender.sendMessage(ChatMsgs.onChannelCommandFail(this.name), false);
			return;
		}
		this.sendToAll(sender, ChatMsgs.onChannelDisband(this.getName()), false);
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			ChatData.removeListeningSilent(User.getUser(userID), this);
		}
		SblockChat.getChat().getChannelManager().dropChannel(this.name);
	}

	public void sendToAll(User sender, String message, boolean format) {
		if (format) {
			message = this.formatMessage(sender, message);
		}
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			User u = User.getUser(userID);
			if (u != null) {
				u.sendMessage(message, sender != null && !userID.equals(sender.getUUID()),
						u.getPlayer().getDisplayName(), this.getNick(u));
			} else {
				listening.remove(userID);
			}
		}
		Log.anonymousInfo(message);
	}

	/**
	 * Gets chat channel name prefix.
	 * 
	 * @param sender the User sending the message
	 * 
	 * @return the channel prefix
	 */
	public String formatMessage(User sender, String message) {

		ChatColor channelRank;
		if (this.isOwner(sender)) {
			channelRank = ColorDef.CHATRANK_OWNER;
		} else if (this.isChannelMod(sender)) {
			channelRank = ColorDef.CHATRANK_MOD;
		} else {
			channelRank = ColorDef.CHATRANK_MEMBER;
		}

		ChatColor globalRank;
		if (sender.getPlayer().hasPermission("group.horrorterror"))
			globalRank = ColorDef.RANK_HORRORTERROR;
		else if (sender.getPlayer().hasPermission("group.denizen"))
			globalRank = ColorDef.RANK_DENIZEN;
		else if (sender.getPlayer().hasPermission("group.felt"))
			globalRank = ColorDef.RANK_FELT;
		else if (sender.getPlayer().hasPermission("group.helper"))
			globalRank = ColorDef.RANK_HELPER;
		else if (sender.getPlayer().hasPermission("group.godtier"))
			globalRank = ColorDef.RANK_GODTIER;
		else if (sender.getPlayer().hasPermission("group.donator"))
			globalRank = ColorDef.RANK_DONATOR;
		else {
			globalRank = ColorDef.RANK_HERO;
		}

		if (this instanceof RPChannel) {
			// guaranteed to have a nick if this point is reached.
			globalRank = CanonNicks.getNick(this.getNick(sender)).getColor();
		}

		// check for third person modifier (#>)
		boolean isThirdPerson = message.length() > 2 && message.charAt(0) == '#'
				&& message.charAt(1) == '>';

		// strip third person modifier from chat
		if (isThirdPerson) {
			message = message.substring(2);
		}

		if (this.getType() == ChannelType.RP) {
			// apply quirk to message
			message = CanonNicks.getNick(this.getNick(sender)).applyQuirk(message);
		} else if (this.isChannelMod(sender)) {
			// color formatting - applies only to channel mods.
			message = ChatColor.translateAlternateColorCodes('&', message);
		}

		ChatColor region = Region.getRegionColor(sender.getCurrentRegion());

		return ChatColor.WHITE + "[" + channelRank + this.name + ChatColor.WHITE + "]" + region
				+ (isThirdPerson ? "> " : " <") + globalRank + this.getNick(sender)
				+ (isThirdPerson ? " " : region + "> ") + ChatColor.WHITE + message;
	}

	public String toString() {
		return ChatColor.GOLD + this.getName() + ChatColor.GREEN + ": Access: " + ChatColor.GOLD
				+ this.getAccess() + ChatColor.GREEN + " Type: " + ChatColor.GOLD + this.getType()
				+ "\n" + ChatColor.GREEN + "Owner: " + ChatColor.GOLD
				+ Bukkit.getOfflinePlayer(this.getOwner()).getName();
	}

}
