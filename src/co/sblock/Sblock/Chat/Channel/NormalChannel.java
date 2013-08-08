package co.sblock.Sblock.Chat.Channel;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Sblogger;

public class NormalChannel implements Channel {

	protected String name;
	protected AccessLevel access;
	protected String owner;

	protected Set<String> approvedList = new HashSet<String>();
	protected Set<String> modList = new HashSet<String>();
	protected Set<String> muteList = new HashSet<String>();
	protected Set<String> banList = new HashSet<String>();

	protected Set<String> listening = new HashSet<String>();

	public NormalChannel(String name, AccessLevel a, String creator) {
		this.name = name;
		this.access = a;
		this.owner = creator;
		this.modList.add(creator);
		DatabaseManager.getDatabaseManager().saveChannelData(this);
		if (this.access.equals(AccessLevel.PRIVATE)) {
			for (String s : modList) {
				approvedList.add(s);
			}
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public AccessLevel getAccess() {
		return this.access;
	}

	@Override
	public Set<String> getListening() {
		return this.listening;
	}

	@Override
	public ChannelType getType() {
		return ChannelType.NORMAL;
	}

	@Override
	public boolean userJoin(SblockUser sender) {
		switch (access) {
		case PUBLIC: {
			if (!banList.contains(sender.getPlayerName())) {
				this.listening.add(sender.getPlayerName());
				this.sendToAll(sender, ChatMsgs.onChannelJoin(sender, this), "channel");
				return true;
			} else {
				sender.sendMessage(ChatMsgs.isBanned(sender, this));
				return false;
			}
		}
		case PRIVATE: {
			if (approvedList.contains(sender.getPlayerName())) {
				this.listening.add(sender.getPlayerName());
				this.sendToAll(sender, ChatMsgs.onChannelJoin(sender, this), "channel");
				return true;
			} else {
				sender.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(sender, this));
				return false;
			}
		}
		default: {
			return false;
		}
		}
	}

	@Override
	public void userLeave(SblockUser sender) {
		this.sendToAll(sender, ChatMsgs.onChannelLeave(sender, this), "channel");
		this.listening.remove(sender);
	}

	@Override
	public void setNick(String nick, SblockUser sender) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(sender, this));
	}

	@Override
	public void removeNick(SblockUser sender) {
		sender.sendMessage(ChatMsgs.unsupportedOperation(sender, this));
	}

	public Nick getNick(SblockUser sender) {
		return new Nick(sender.getNick());
	}

	@Override
	public void setOwner(String newO, SblockUser sender) {
		if (sender.equals(this.owner)) {
			this.owner = newO;
		}
	}

	@Override
	public String getOwner() {
		return this.owner;
	}

	@Override
	public boolean isOwner(SblockUser user) {
		return user.getPlayerName().equalsIgnoreCase(owner);
	}

	@Override
	public void loadMod(String user) {
		this.modList.add(user);
	}

	@Override
	public void addMod(String username, SblockUser sender) {
		// SburbChat code. Handle with care

		if(!Bukkit.getOfflinePlayer(username).hasPlayedBefore()) {
			sender.sendMessage(ChatColor.YELLOW + username
					+ ChatColor.RED + " does not exist! Get them to log in once.");
		}
		if (modList.contains(sender.getPlayerName())
				&& !modList.contains(username)) {
			this.modList.add(username);
			if (this.access.equals(AccessLevel.PRIVATE)) {
				this.approvedList.add(username);
			}
			this.sendToAll(sender, ChatColor.YELLOW + username
					+ " is now a mod in " + ChatColor.GOLD + this.name
					+ ChatColor.YELLOW + "!", "channel");
			Player targetUser = Bukkit.getPlayerExact(username);
			if (targetUser != null) {
				targetUser.sendMessage(ChatColor.GREEN
					+ "You are now a mod in " + ChatColor.GOLD
					+ this.name + ChatColor.GREEN + "!");
			}
		} else if (!sender.getPlayerName().equals(owner)) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission to mod people in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else {
			sender.sendMessage(ChatColor.YELLOW + username
					+ ChatColor.RED + " is already a mod in " + ChatColor.GOLD
					+ this.name + ChatColor.RED + "!");
		}

	}

	@Override
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
				targetUser.sendMessage(ChatColor.RED + "You are no longer a mod in "
						+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
			}
		} else if (!sender.getPlayerName().equals(this.owner)) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission to demod people in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else {
			sender.sendMessage(ChatColor.YELLOW + target
					+ ChatColor.RED + " is not a mod in " + ChatColor.GOLD
					+ this.name + ChatColor.RED + "!");
		}
	}

	@Override
	public Set<String> getModList() {
		return this.modList;
	}

	@Override
	public boolean isChannelMod(SblockUser user)	{
		if(modList.contains(user.getPlayerName())
				|| user.getPlayer().hasPermission("group.denizen")
				|| user.getPlayer().hasPermission("group.horrorterror"))	{
			return true;
		}
		return false;
	}

	@Override
	public boolean isMod(SblockUser user) {
		if (user.getPlayer().hasPermission("group.denizen")
				|| user.getPlayer().hasPermission("group.horrorterror")) {
			return true;
		}
		return false;
	}

	@Override
	public void kickUser(SblockUser user, SblockUser sender) {
		// SburbChat code. Handle with care
		if (modList.contains(sender.getPlayerName())
				&& listening.contains(user)) {
			this.listening.remove(user);
			user.sendMessage(ChatColor.YELLOW + "You have been kicked from "
					+ ChatColor.GOLD + this.getName() + ChatColor.YELLOW + "!");
			user.removeListening(this);
			this.sendToAll(
					sender,
					ChatColor.YELLOW + user.getPlayerName()
							+ " has been kicked from " + ChatColor.GOLD
							+ this.getName() + ChatColor.YELLOW + "!", "channel");
		} else if (!modList.contains(sender.getPlayerName())) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission to kick people in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else {
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName()
					+ ChatColor.RED + " is not chatting in " + ChatColor.GOLD
					+ this.name + ChatColor.RED + "!");
		}

	}

	@Override
	public void loadBan(String user) {
		this.banList.add(user);
	}

	@Override
	public void banUser(SblockUser user, SblockUser sender) {
		if (this.isMod(sender) && !banList.contains(user.getPlayerName())) {
			if (modList.contains(user)) {
				modList.remove(user);
			}
			if (listening.contains(user)) {
				this.listening.remove(user);
				user.removeListening(this);
			}
			this.banList.add(user.getPlayerName());
			user.sendMessage(ChatColor.RED + "You have been " + ChatColor.BOLD
					+ "banned" + ChatColor.RESET + ChatColor.RED + " from "
					+ ChatColor.GOLD + this.getName() + ChatColor.RED + "!");
			this.sendToAll(sender,
					ChatColor.YELLOW + user.getPlayerName() + ChatColor.RED
							+ " has been " + ChatColor.BOLD + "banned"
							+ ChatColor.RESET + " from " + ChatColor.GOLD
							+ this.getName() + ChatColor.RED + "!", "channel");
		} else if (!sender.getPlayerName().equalsIgnoreCase(owner)) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission to ban people in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else {
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName()
					+ ChatColor.RED + " is already banned in " + ChatColor.GOLD
					+ this.name + ChatColor.RED + "!");
		}
	}

	@Override
	public void unbanUser(SblockUser user, SblockUser sender) {
		if (sender.getPlayerName().equalsIgnoreCase(this.owner)
				&& banList.contains(user.getPlayerName())) {
			this.banList.remove(user.getPlayerName());
			user.sendMessage(ChatColor.RED + "You have been " + ChatColor.BOLD
					+ "unbanned" + ChatColor.RESET + " from " + ChatColor.GOLD
					+ this.getName() + ChatColor.RED + "!");
			this.sendToAll(sender, ChatColor.YELLOW + user.getPlayerName()
					+ ChatColor.RED + " has been " + ChatColor.BOLD
					+ "unbanned" + ChatColor.RESET + " from " + ChatColor.GOLD
					+ this.getName() + ChatColor.RED + "!", "channel");
		} else if (!sender.getPlayerName().equalsIgnoreCase(owner)) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission to unban people in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else {
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName()
					+ ChatColor.RED + " is not banned in " + ChatColor.GOLD
					+ this.name + ChatColor.RED + "!");
		}
	}

	@Override
	public Set<String> getBanList() {
		return banList;
	}

	@Override
	public boolean isBanned(SblockUser user) {
		return banList.contains(user.getPlayerName());
	}

	@Override
	public void loadApproval(String user) {
		// Public channel; do nothing.
		//^False, Normal does not imply public
	}

	@Override
	public void approveUser(SblockUser user, SblockUser sender) {
		if(this.getAccess().equals(AccessLevel.PUBLIC))	{
			sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED
					+ " is a public channel!");
			return;
		}
		else	{
			approvedList.add(user.getPlayerName());
		}
	}

	@Override
	public void deapproveUser(SblockUser user, SblockUser sender) {
		if(this.getAccess().equals(AccessLevel.PUBLIC))	{
			sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED
					+ " is a public channel!");
			return;
		}
		else	{
			approvedList.remove(user.getPlayerName());
		}
	}

	public Set<String> getApprovedUsers() {
		return approvedList;
	}
	@Override
	public boolean isApproved(SblockUser user)	{
		return approvedList.contains(user.getPlayerName());
	}

	@Override
	public void disband(SblockUser sender) {
		this.sendToAll(sender, ChatColor.GOLD + this.name + ChatColor.RED
				+ " has been disbanded! These are indeed dark times...", "channel");
		for (String s : this.listening) {
			UserManager.getUserManager().getUser(s).removeListening(this);
			this.listening.remove(s);
		}
		ChatModule.getInstance().getChannelManager().dropChannel(this.name);
	}

	@Override
	public void sendToAll(SblockUser sender, String s, String type) {
		// TODO temporary fix for onPlayerQuit not firing in correct order
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
