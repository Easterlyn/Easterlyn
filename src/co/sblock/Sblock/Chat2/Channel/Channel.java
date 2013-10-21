package co.sblock.Sblock.Chat2.Channel;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;
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
//		DatabaseManager.getDatabaseManager().saveChannelData(this);
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

	public ChannelType getType() {
		return null;
	}

	
	public void addListening(String user) {
		this.listening.add(user);
	}

	public void removeListening(String user) {
		this.listening.remove(user);
	}

	
	public void setNick(SblockUser sender, String nick) {
		//Channel-Specific
	}

	public void removeNick(SblockUser sender) {
		//Channel-specific
	}

	public String getNick(SblockUser sender) {
		//Channel-specific
	}
	
	public boolean hasNick(SblockUser sender)	{
		//Channel-specific
	}

	
	public void setOwner(SblockUser sender, String newO) {
		if (sender.equals(this.owner)) {
			this.owner = newO;
		}
	}

	public String getOwner() {
		return this.owner;
	}

	public boolean isOwner(SblockUser user) {
		return user.getPlayerName().equalsIgnoreCase(owner);
	}

	//db method
	public void loadMod(String user) {
		this.modList.add(user);
	}

	public void addMod(SblockUser sender, String username) {
		if (!SblockUser.isValidUser(username)) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(username));
			return;
		}
		if (this.isChannelMod(sender) && !modList.contains(username)) {
			this.modList.add(username);
			this.sendToAll(sender,
					ChatMsgs.onUserModAnnounce(username, this), "channel");
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

	
	public void removeMod(String target, SblockUser sender) {
		// SburbChat code. Handle with care

		if (modList.contains(sender.getPlayerName())
				&& this.modList.contains(target)) {
			this.modList.remove(target);
			this.sendToAll(sender, ChatColor.YELLOW + target
					+ " is no longer a mod in " + ChatColor.GOLD + this.name
					+ ChatColor.YELLOW + "!", "channel");
			Player targetUser = Bukkit.getPlayerExact(target);
			if (targetUser != null) {
				targetUser.sendMessage(ChatColor.RED
						+ "You are no longer a mod in " + ChatColor.GOLD
						+ this.name + ChatColor.RED + "!");
			}
		} else if (!sender.getPlayerName().equals(this.owner)) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission to demod people in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else {
			sender.sendMessage(ChatColor.YELLOW + target + ChatColor.RED
					+ " is not a mod in " + ChatColor.GOLD + this.name
					+ ChatColor.RED + "!");
		}
	}

	
	public Set<String> getModList() {
		return this.modList;
	}

	
	public boolean isChannelMod(SblockUser user) {
		if (modList.contains(user.getPlayerName())
				|| user.getPlayer().hasPermission("group.denizen")
				|| user.getPlayer().hasPermission("group.horrorterror")) {
			return true;
		}
		return false;
	}

	
	public boolean isMod(SblockUser user) {
		if (user.getPlayer().hasPermission("group.denizen")
				|| user.getPlayer().hasPermission("group.horrorterror")) {
			return true;
		}
		return false;
	}

	
	public void kickUser(SblockUser user, SblockUser sender) {
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

	
	public void loadBan(String user) {
		this.banList.add(user);
	}

	
	public void banUser(String username, SblockUser sender) {
		if (this.isChannelMod(sender)
				&& !banList.contains(username)) {
			if (modList.contains(username)) {
				modList.remove(username);
			}
			if (listening.contains(username)) {
				SblockUser user = SblockUser.getUser(username);
				if (user != null) {
					user.removeListening(this.getName());
					user.sendMessage(ChatMsgs.onUserBan(this));
				}
			}
			this.banList.add(username);
			this.sendToAll(sender, ChatMsgs.onUserBanAnnounce(username, this),
					"channel");
		} else if (!this.isChannelMod(sender)) {
			sender.sendMessage(ChatMsgs.onUserBanFail(this));
		} else {
			sender.sendMessage(ChatMsgs.onUserBannedAlready(username, this));
		}
	}

	
	public void unbanUser(String username, SblockUser sender) {
		if (sender.getPlayerName().equalsIgnoreCase(this.owner)
				&& banList.contains(username)) {
			this.banList.remove(username);
			Player user = Bukkit.getPlayerExact(username);
			if (user != null) {
				user.sendMessage(ChatMsgs.onUserUnban(this));
			}
			this.sendToAll(sender, ChatMsgs.onUserUnbanAnnounce(username, this),
					"channel");
		} else if (!sender.getPlayerName().equalsIgnoreCase(owner)) {
			sender.sendMessage(ChatMsgs.onUserUnbanFail(this));
		} else {
			sender.sendMessage(ChatMsgs.onUserUnbannedAlready(username, this));
		}
	}

	
	public Set<String> getBanList() {
		return banList;
	}

	
	public boolean isBanned(SblockUser user) {
		return banList.contains(user.getPlayerName());
	}

	
	public void loadApproval(String user) {
		this.approvedList.add(user);
	}

	
	public void approveUser(SblockUser user, SblockUser sender) {
		if (this.getAccess().equals(AccessLevel.PUBLIC)) {
			sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED
					+ " is a public channel!");
			return;
		} else {
			approvedList.add(user.getPlayerName());
		}
	}

	
	public void deapproveUser(SblockUser user, SblockUser sender) {
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

	
	public boolean isApproved(SblockUser user) {
		return approvedList.contains(user.getPlayerName())
				|| isChannelMod(user);
	}

	
	public void disband(SblockUser sender) {
		this.sendToAll(sender, ChatMsgs.onChannelDisband(this.getName()), "channel");
		for (String s : this.listening) {
			UserManager.getUserManager().getUser(s).removeListening(this.getName());
		}
		ChatModule.getChatModule().getChannelManager().dropChannel(this.name);
	}

	
	public void sendToAll(SblockUser sender, String s, String type) {
		Set<String> failures = new HashSet<String>();
		for (String name : this.listening) {
			SblockUser u = UserManager.getUserManager().getUser(name);
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

	
	
}
