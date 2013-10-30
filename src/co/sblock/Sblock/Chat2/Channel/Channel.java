 package co.sblock.Sblock.Chat2.Channel;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Chat2.ChatModule;
import co.sblock.Sblock.Chat2.ChatMsgs;
import co.sblock.Sblock.Chat2.ChatUser;
import co.sblock.Sblock.Chat2.ChatUserManager;
import co.sblock.Sblock.Chat2.Channel.AccessLevel;
import co.sblock.Sblock.Chat2.Channel.ChannelType;
import co.sblock.Sblock.Utilities.Sblogger;
/**
 * Defines default channel behavior
 * 
 * @author Dublek
 */
public abstract class Channel {

	protected String name;
	protected AccessLevel access;
	protected String owner;

	protected Set<String> approvedList = new HashSet<String>();
	protected Set<String> modList = new HashSet<String>();
	protected Set<String> muteList = new HashSet<String>();
	protected Set<String> banList = new HashSet<String>();
	protected Set<String> listening = new HashSet<String>();
	
	
	public Channel(String name, AccessLevel a, String creator) {
		this.name = name;
		this.access = a;
		this.owner = creator;
		this.modList.add(creator);
		DatabaseManager.getDatabaseManager().saveChannelData(this);
	}

	public String getName() {
		return this.name;
	}	

	public AccessLevel getAccess() {
		return this.access;
	}

	public Set<String> getListening() {
		return this.listening;
	}

	public abstract ChannelType getType();
	/**
	 * ONLY CALL FROM CHATUSER
	 * 
	 * @param user
	 *            the name to add to listening.
	 */
	public void addListening(String user) {
		this.listening.add(user);
	}
	/**
	 * ONLY CALL FROM CHATUSER
	 * 
	 * @param user
	 *            the name to remove from listening.
	 */
	public void removeListening(String user) {
		this.listening.remove(user);
	}

	public abstract void setNick(ChatUser sender, String nick);

	public abstract void removeNick(ChatUser sender);

	public abstract String getNick(ChatUser sender);

	public abstract boolean hasNick(ChatUser sender);

	public abstract ChatUser getNickOwner(String nick);
	
	public void setOwner(ChatUser sender, String newO) {
		if (sender.equals(this.owner)) {
			this.owner = newO;
		}
	}

	public String getOwner() {
		return this.owner;
	}

	public boolean isOwner(ChatUser user) {
		return user.getPlayerName().equalsIgnoreCase(owner);
	}

	/**
	 * Method used by database to load a moderator silently.
	 * 
	 * @param user
	 *            the name to add as a moderator
	 */
	public void loadMod(String user) {
		this.modList.add(user);
	}

	public void addMod(ChatUser sender, String username) {
		if (!ChatUser.isValidUser(username)) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(username));
			return;
		}
		if (this.isChannelMod(sender) && !this.isChannelMod(ChatUser.getUser(username))) {
			this.modList.add(username);
			this.sendToAll(sender, ChatMsgs.onUserModAnnounce(username, this), "channel");
			Player targetUser = Bukkit.getPlayerExact(username);
			if (targetUser != null) {
				targetUser.sendMessage(ChatMsgs.onUserMod(this));
			}
		} else if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onUserModFail(this));
		} else {
			sender.sendMessage(ChatMsgs.onUserModAlready(username, this));
		}
	}

	public void removeMod(ChatUser sender, String target) {
		if (this.isChannelMod(sender) && this.isChannelMod(ChatUser.getUser(target))) {
			this.modList.remove(target);
			this.sendToAll(sender, ChatMsgs.onUserRmModAnnounce(target, this), "channel");
			Player targetUser = Bukkit.getPlayerExact(target);
			if (targetUser != null) {
				targetUser.sendMessage(ChatMsgs.onUserRmMod(target, this)); 
			}
		} else if (!sender.getPlayerName().equals(this.owner)) {
			sender.sendMessage(ChatMsgs.onUserModFail(this));
		} else {
			sender.sendMessage(ChatColor.YELLOW + target + ChatColor.RED
					+ " is not a mod in " + ChatColor.GOLD + this.name
					+ ChatColor.RED + "!");
		}
	}

	public Set<String> getModList() {
		return this.modList;
	}

	public boolean isChannelMod(ChatUser user) {
		if (modList.contains(user.getPlayerName()) || isMod(user)) {
			return true;
		}
		return false;
	}

	public boolean isMod(ChatUser user) {
		if (user.getPlayer().hasPermission("group.denizen")
				|| user.getPlayer().hasPermission("group.horrorterror")) {
			return true;
		}
		return false;
	}

	public void kickUser(ChatUser user, ChatUser sender) {
		if (this.isChannelMod(sender) && listening.contains(user.getPlayerName())) {
			this.listening.remove(user);
			user.sendMessage(ChatMsgs.onUserKick(this));
			user.removeListening(this.getName());
			this.sendToAll(sender, ChatMsgs.onUserKickAnnounce(user, this), "channel");
		} else if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onUserKickFail(this));
		} else {
			sender.sendMessage(ChatMsgs.onUserKickedAlready(user, this));
		}

	}
	/**
	 * Method used by database to load a ban silently.
	 * 
	 * @param user
	 *            the name to add as a ban
	 */
	public void loadBan(String user) {
		this.banList.add(user);
	}

	public void banUser(String username, ChatUser sender) {
		if (this.isChannelMod(sender) && this.isBanned(ChatUser.getUser(username))) {
			if (modList.contains(username)) {
				modList.remove(username);
			}
			if (listening.contains(username)) {
				ChatUser user = ChatUser.getUser(username);
				if (user != null) {
					user.removeListening(this.getName());
					user.sendMessage(ChatMsgs.onUserBan(this));
				}
			}
			this.banList.add(username);
			this.sendToAll(sender, ChatMsgs.onUserBanAnnounce(username, this), "channel");
		} else if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onUserBanFail(this));
		} else {
			sender.sendMessage(ChatMsgs.onUserBannedAlready(username, this));
		}
	}

	public void unbanUser(String username, ChatUser sender) {
		if (sender.getPlayerName().equalsIgnoreCase(this.owner)	&& banList.contains(username)) {
			this.banList.remove(username);
			Player user = Bukkit.getPlayerExact(username);
			if (user != null) {
				user.sendMessage(ChatMsgs.onUserUnban(this));
			}
			this.sendToAll(sender, ChatMsgs.onUserUnbanAnnounce(username, this), "channel");
		} else if (!sender.getPlayerName().equalsIgnoreCase(owner)) {
			sender.sendMessage(ChatMsgs.onUserUnbanFail(this));
		} else {
			sender.sendMessage(ChatMsgs.onUserUnbannedAlready(username, this));
		}
	}

	public Set<String> getBanList() {
		return banList;
	}

	public boolean isBanned(ChatUser user) {
		return banList.contains(user.getPlayerName());
	}

	public void loadApproval(String user) {
		this.approvedList.add(user);
	}

	public void approveUser(ChatUser user, ChatUser sender) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED
					+ " is a public channel!");
			return;
		} else {
			approvedList.add(user.getPlayerName());
		}
	}

	public void deapproveUser(ChatUser user, ChatUser sender) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED
					+ " is a public channel!");
			return;
		} else {
			approvedList.remove(user.getPlayerName());
		}
	}

	public Set<String> getApprovedUsers() {
		return approvedList;
	}

	public boolean isApproved(ChatUser user) {
		return approvedList.contains(user.getPlayerName()) || isChannelMod(user);
	}

	public void disband(ChatUser sender) {
		this.sendToAll(sender, ChatMsgs.onChannelDisband(this.getName()), "channel");
		for (String s : this.listening) {
			ChatUserManager.getUserManager().getUser(s).removeListening(this.getName());
		}
		ChatModule.getChatModule().getChannelManager().dropChannel(this.name);
	}

	public void sendToAll(ChatUser sender, String s, String type) {
		Set<String> failures = new HashSet<String>();
		for (String name : this.listening) {
			ChatUser u = ChatUserManager.getUserManager().getUser(name);
			if (u != null) {
				u.sendMessageFromChannel(s, this, type);
			} else {
				failures.add(name);
			}
		}
		for (String failure : failures) {
			this.listening.remove(failure);
		}
		Sblogger.infoNoLogName(s);
	}

}
