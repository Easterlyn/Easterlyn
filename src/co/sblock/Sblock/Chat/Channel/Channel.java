 package co.sblock.Sblock.Chat.Channel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Chat.SblockChat;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.Utilities.Log;
/**
 * Defines default channel behavior
 * 
 * @author Dublek
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
		this.modList.add(creator);
		approvedList = new HashSet<>();
		modList = new HashSet<>();
		muteList = new HashSet<>();
		banList = new HashSet<>();
		listening = new HashSet<>();
		SblockData.getDB().saveChannelData(this);
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

	public abstract void setNick(ChatUser sender, String nick);

	public abstract void removeNick(ChatUser sender);

	public abstract String getNick(ChatUser sender);

	public abstract boolean hasNick(ChatUser sender);

	public abstract ChatUser getNickOwner(String nick);
	
	public void setOwner(ChatUser sender, UUID newOwner) {
		if (sender.equals(this.owner)) {
			this.owner = newOwner;
		}
	}

	public UUID getOwner() {
		return this.owner;
	}

	public boolean isOwner(ChatUser user) {
		return user.getPlayerName().equalsIgnoreCase(Bukkit.getOfflinePlayer(owner).getName())
				|| user.getPlayer().hasPermission("group.denizen");
	}

	/**
	 * Method used by database to load a moderator silently.
	 * 
	 * @param user the name to add as a moderator
	 */
	public void loadMod(UUID id) {
		this.modList.add(id);
	}

	public void addMod(ChatUser sender, UUID userID) {
		if (ChatUser.getUser(userID) == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(userID.toString()));
			return;
		}
		if (this.isChannelMod(sender) && !this.isChannelMod(ChatUser.getUser(userID))) {
			Player p = Bukkit.getPlayer(userID);
			this.modList.add(p.getUniqueId());
			this.sendToAll(sender, ChatMsgs.onUserModAnnounce(p.getName(), this.name), "channel");
			p.sendMessage(ChatMsgs.onUserMod(this.name));
		} else if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onUserModFail(this.name));
		} else {
			sender.sendMessage(ChatMsgs.onUserModAlready(Bukkit.getOfflinePlayer(userID).getName(), this.name));
		}
	}

	public void removeMod(ChatUser sender, UUID userID) {
		if (this.isChannelMod(sender) && this.isChannelMod(ChatUser.getUser(userID))) {
			this.modList.remove(userID);
			this.sendToAll(sender, ChatMsgs.onUserRmModAnnounce(
					Bukkit.getOfflinePlayer(userID).getName(), this.name), "channel");
			Player targetUser = Bukkit.getPlayer(userID);
			if (targetUser != null) {
				targetUser.sendMessage(ChatMsgs.onUserRmMod(targetUser.getName(), this.name)); 
			}
		} else if (!sender.getPlayerName().equals(this.owner)) {
			sender.sendMessage(ChatMsgs.onUserModFail(this.name));
		} else {
			sender.sendMessage(ChatColor.YELLOW + Bukkit.getOfflinePlayer(userID).getName()
					+ ChatColor.RED + " is not a mod in " + ChatColor.GOLD + this.name
					+ ChatColor.RED + "!");
		}
	}

	public Set<UUID> getModList() {
		return this.modList;
	}

	public boolean isChannelMod(ChatUser user) {
		return isMod(user) || user.getPlayer().hasPermission("group.felt");
	}

	public boolean isMod(ChatUser user) {
		return modList.contains(user.getUUID()) || user.getPlayer().hasPermission("group.denizen");
	}

	public void kickUser(ChatUser user, ChatUser sender) {
		if (this.isChannelMod(sender) && listening.contains(user.getPlayerName())) {
			this.listening.remove(user.getUUID());
			user.sendMessage(ChatMsgs.onUserKick(this.name));
			user.removeListening(this.getName());
			this.sendToAll(sender, ChatMsgs.onUserKickAnnounce(user.getPlayerName(), this.name), "channel");
		} else if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onUserKickFail(this.name));
		} else {
			sender.sendMessage(ChatMsgs.onUserKickedAlready(user.getPlayerName(), this.name));
		}

	}
	/**
	 * Method used by database to load a ban silently.
	 * 
	 * @param user
	 *            the name to add as a ban
	 */
	public void loadBan(UUID userID) {
		this.banList.add(userID);
	}

	public void banUser(UUID userID, ChatUser sender) {
		if (this.isChannelMod(sender) && this.isBanned(ChatUser.getUser(userID)) && !userID.equals(owner)) {
			if (modList.contains(userID)) {
				modList.remove(userID);
			}
			if (listening.contains(userID)) {
				ChatUser user = ChatUser.getUser(userID);
				if (user != null) {
					user.removeListening(this.getName());
					user.sendMessage(ChatMsgs.onUserBan(this.name));
				}
			}
			this.banList.add(userID);
			this.sendToAll(sender, ChatMsgs.onUserBanAnnounce(Bukkit.getPlayer(userID).getName(), this.name), "channel");
		} else if (!this.isChannelMod(sender) || userID.equals(owner)) {
			sender.sendMessage(ChatMsgs.onUserBanFail(this.name));
		} else {
			sender.sendMessage(ChatMsgs.onUserBannedAlready(Bukkit.getPlayer(userID).getName(), this.name));
		}
	}

	public void unbanUser(UUID userID, ChatUser sender) {
		OfflinePlayer user = Bukkit.getOfflinePlayer(userID);
		if (sender.getUUID().equals(this.owner)	&& banList.contains(userID)) {
			this.banList.remove(userID);
			if (user.isOnline()) {
				user.getPlayer().sendMessage(ChatMsgs.onUserUnban(this.name));
			}
			this.sendToAll(sender, ChatMsgs.onUserUnbanAnnounce(user.getName(), this.name), "channel");
		} else if (!sender.getUUID().equals(owner)) {
			sender.sendMessage(ChatMsgs.onUserUnbanFail(this.name));
		} else {
			sender.sendMessage(ChatMsgs.onUserUnbannedAlready(user.getName(), this.name));
		}
	}

	public Set<UUID> getBanList() {
		return banList;
	}

	public boolean isBanned(ChatUser user) {
		return banList.contains(user.getUUID());
	}

	public void loadApproval(UUID userID) {
		this.approvedList.add(userID);
	}

	public void approveUser(ChatUser user, ChatUser sender) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED + " is a public channel!");
			return;
		} else {
			approvedList.add(user.getUUID());
		}
	}

	public void deapproveUser(ChatUser user, ChatUser sender) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED + " is a public channel!");
			return;
		} else {
			approvedList.remove(user.getUUID());
		}
	}

	public Set<UUID> getApprovedUsers() {
		return approvedList;
	}

	public boolean isApproved(ChatUser user) {
		return approvedList.contains(user.getUUID()) || isChannelMod(user);
	}

	public void disband(ChatUser sender) {
		this.sendToAll(sender, ChatMsgs.onChannelDisband(this.getName()), "channel");
		for (UUID userID : this.listening) {
			ChatUserManager.getUserManager().getUser(userID).removeListening(this.getName());
		}
		SblockChat.getChat().getChannelManager().dropChannel(this.name);
	}

	public void sendToAll(ChatUser sender, String s, String type) {
		for (UUID userID : this.listening.toArray(new UUID[0])) {
			ChatUser u = ChatUserManager.getUserManager().getUser(userID);
			if (u != null) {
				u.sendMessageFromChannel(s, this, type);
			} else {
				listening.remove(userID);
			}
		}
		Log.anonymousInfo(s);
	}
	
	public String toString()	{
		return ChatColor.GOLD + this.getName() + ChatColor.GREEN + ": Access: " + ChatColor.GOLD
				+ this.getAccess() + ChatColor.GREEN + " Type: " + ChatColor.GOLD + this.getType()
				+ "\n" + ChatColor.GREEN + "Owner: " + ChatColor.GOLD
				+ Bukkit.getOfflinePlayer(this.getOwner()).getName();
	}

}
