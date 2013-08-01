package co.sblock.Sblock.Chat.Channel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.ChatColor;

import com.google.common.collect.HashBiMap;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.UserData.SblockUser;

/**
 * @author Jikoo
 *
 */
public class RPChannel implements Channel {

	protected String name;
	protected HashBiMap<String, CanonNicks> nickList;
	protected ChannelType type = ChannelType.RP;
	protected AccessLevel access;
	protected String owner;

	protected List<String> approvedList = new ArrayList<String>();
	protected List<String> modList = new ArrayList<String>();
	protected List<String> muteList = new ArrayList<String>();
	protected List<String> banList = new ArrayList<String>();

	protected List<String> listening = new ArrayList<String>();

	public RPChannel(String name, AccessLevel a, String creator) {
		this.name = name;
		this.nickList = HashBiMap.create();
		this.access = a;
		this.owner = creator;
		this.modList.add(creator);
		DatabaseManager.getDatabaseManager().saveChannelData(this);
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getJoinChatMessage(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public String getJoinChatMessage(SblockUser sender) {
		StringBuilder joinMessage = new StringBuilder();
		CanonNicks nick = getNick(sender);
		joinMessage.append(nick.getName() + ChatColor.YELLOW + " began ");
		joinMessage.append(nick.getPester() + " ");
		joinMessage.append(ChatColor.GOLD + this.getName());
		joinMessage.append(" at " + new SimpleDateFormat("HH:mm").format(new Date()));
		return joinMessage.toString();
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getLeaveChatMessage(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public String getLeaveChatMessage(SblockUser sender) {
		return getJoinChatMessage(sender).replaceAll("began", "ceased");
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getListening()
	 */
	@Override
	public List<String> getListening() {
		return this.listening;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getType()
	 */
	@Override
	public ChannelType getType() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#userJoin(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public boolean userJoin(SblockUser sender) {
		switch (access) {
		case PUBLIC: {
			if (!banList.contains(sender.getPlayerName())) {
				this.listening.add(sender.getPlayerName());
				this.sendToAll(sender, this.getJoinChatMessage(sender));
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "You are banned from "
						+ ChatColor.GOLD + this.name + ChatColor.RED + "!");
				return false;
			}
		}
		case PRIVATE: {
			if (approvedList.contains(sender.getPlayerName())) {
				this.listening.add(sender.getPlayerName());
				this.sendToAll(sender, this.getJoinChatMessage(sender));
				return true;
			} else {
				sender.sendMessage(ChatColor.GOLD + this.name + ChatColor.RED
						+ " is a " + ChatColor.BOLD + "private"
						+ ChatColor.RESET + " channel!");
				return false;
			}
		}
		default: {
			return false;
		}
		}
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#userLeave(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void userLeave(SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#setNick(java.lang.String, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void setNick(String nick, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#removeNick(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void removeNick(SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getNick(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public CanonNicks getNick(SblockUser sender) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#setOwner(java.lang.String, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void setOwner(String name, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getOwner()
	 */
	@Override
	public String getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#loadMod(java.lang.String)
	 */
	@Override
	public void loadMod(String user) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#addMod(co.sblock.Sblock.UserData.SblockUser, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void addMod(SblockUser user, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#removeMod(co.sblock.Sblock.UserData.SblockUser, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void removeMod(SblockUser user, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getModList()
	 */
	@Override
	public List<String> getModList() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#isMod(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public boolean isMod(SblockUser user) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#kickUser(co.sblock.Sblock.UserData.SblockUser, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void kickUser(SblockUser user, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#loadBan(java.lang.String)
	 */
	@Override
	public void loadBan(String user) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#banUser(co.sblock.Sblock.UserData.SblockUser, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void banUser(SblockUser user, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#unbanUser(co.sblock.Sblock.UserData.SblockUser, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void unbanUser(SblockUser user, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#isBanned(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public boolean isBanned(SblockUser user) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#loadApproval(java.lang.String)
	 */
	@Override
	public void loadApproval(String user) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#approveUser(co.sblock.Sblock.UserData.SblockUser, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void approveUser(SblockUser user, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#deapproveUser(co.sblock.Sblock.UserData.SblockUser, co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void deapproveUser(SblockUser user, SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getApprovedUsers()
	 */
	@Override
	public List<String> getApprovedUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#disband(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public void disband(SblockUser sender) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#sendToAll(co.sblock.Sblock.UserData.SblockUser, java.lang.String)
	 */
	@Override
	public void sendToAll(SblockUser sender, String message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#getBanList()
	 */
	@Override
	public List<String> getBanList() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Chat.Channel.Channel#isOwner(co.sblock.Sblock.UserData.SblockUser)
	 */
	@Override
	public boolean isOwner(SblockUser user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AccessLevel getAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isChannelMod(SblockUser user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isApproved(SblockUser user) {
		// TODO Auto-generated method stub
		return false;
	}
}
