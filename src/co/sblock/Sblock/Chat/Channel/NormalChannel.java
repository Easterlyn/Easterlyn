package co.sblock.Sblock.Chat.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

public class NormalChannel implements Channel {

	protected String name;
	protected AccessLevel access;
	protected String owner;

	protected List<String> approvedList = new ArrayList<String>();
	protected List<String> modList = new ArrayList<String>();
	protected List<String> muteList = new ArrayList<String>();
	protected List<String> banList = new ArrayList<String>();

	protected List<String> listening = new ArrayList<String>();

	public NormalChannel(String name, AccessLevel a, String creator) {
		this.name = name;
		this.access = a;
		this.owner = creator;
		this.modList.add(creator);
		DatabaseManager.getDatabaseManager().saveChannelData(this);
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
	public List<String> getListening() {
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
				this.sendToAll(sender, ChatMsgs.onChannelJoin(sender, this));
				return true;
			} else {
				sender.sendMessage(ChatMsgs.isBanned(sender, this));
				return false;
			}
		}
		case PRIVATE: {
			if (approvedList.contains(sender.getPlayerName())) {
				this.listening.add(sender.getPlayerName());
				this.sendToAll(sender, ChatMsgs.onChannelJoin(sender, this));
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
		this.sendToAll(sender, ChatMsgs.onChannelLeave(sender, this));
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

	public CanonNicks getNick(SblockUser sender) {
		return CanonNicks.CUSTOM.customize(sender.getNick(), null);
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
	public void addMod(SblockUser user, SblockUser sender) {
		// SburbChat code. Handle with care

		if (modList.contains(sender.getPlayerName())
				&& !modList.contains(user.getPlayerName())) {
			this.modList.add(user.getPlayerName());
			this.sendToAll(sender, ChatColor.YELLOW + user.getPlayerName()
					+ " is now a mod in " + ChatColor.GOLD + this.name
					+ ChatColor.YELLOW + "!");
			user.sendMessage(ChatColor.GREEN + "You are now a mod in "
					+ ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
		} else if (!sender.getPlayerName().equals(owner)) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission to mod people in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else {
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName()
					+ ChatColor.RED + " is already a mod in " + ChatColor.GOLD
					+ this.name + ChatColor.RED + "!");
		}

	}

	@Override
	public void removeMod(SblockUser user, SblockUser sender) {
		// SburbChat code. Handle with care

		if (modList.contains(sender.getPlayerName())
				&& this.modList.contains(user.getPlayerName())) {
			this.modList.remove(user.getPlayerName());
			this.sendToAll(sender, ChatColor.YELLOW + user.getPlayerName()
					+ " is no longer a mod in " + ChatColor.GOLD + this.name
					+ ChatColor.YELLOW + "!");
			user.sendMessage(ChatColor.RED + "You are no longer a mod in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else if (!sender.getPlayerName().equals(this.owner)) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission to demod people in "
					+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
		} else {
			sender.sendMessage(ChatColor.YELLOW + user.getPlayerName()
					+ ChatColor.RED + " is not a mod in " + ChatColor.GOLD
					+ this.name + ChatColor.RED + "!");
		}
	}

	@Override
	public List<String> getModList() {
		return this.modList;
	}
	@Override
	public boolean isChannelMod(SblockUser user)	{
		if(modList.contains(user.getPlayerName()))	{
			return true;
		}
		return false;
	}

	@Override
	public boolean isMod(SblockUser user) {
		if (modList.contains(user.getPlayerName())
				|| user.getPlayer().hasPermission("group.denizen")
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
							+ this.getName() + ChatColor.YELLOW + "!");
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
							+ this.getName() + ChatColor.RED + "!");
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
					+ this.getName() + ChatColor.RED + "!");
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
	public List<String> getBanList() {
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

	public List<String> getApprovedUsers() {
		return approvedList;
	}
	@Override
	public boolean isApproved(SblockUser user)	{
		return approvedList.contains(user.getPlayerName());
	}

	@Override
	public void disband(SblockUser sender) {
		this.sendToAll(sender, ChatColor.GOLD + this.name + ChatColor.RED
				+ " has been disbanded! These are indeed dark times...");
		for (String s : this.listening) {
			UserManager.getUserManager().getUser(s).removeListening(this);
			this.listening.remove(s);
		}
		ChatModule.getInstance().getChannelManager().dropChannel(this.name);
	}

	@Override
	public void sendToAll(SblockUser sender, String s) {
		for (String name : this.listening) {
			UserManager.getUserManager().getUser(name).sendMessageFromChannel(s, this);
		}
		Logger.getLogger("Minecraft").info(ChatColor.stripColor(s));
	}

}
