package co.sblock.Sblock.Chat.Channel;

import java.util.List;

import co.sblock.Sblock.UserData.SblockUser;

public interface Channel {
	public String getName();
	public String getJoinChatMessage(SblockUser sender);
	public String getLeaveChatMessage(SblockUser sender);
	public AccessLevel getAccess();
	public List<String> getListening();
	public ChannelType getType();
	
	public boolean userJoin(SblockUser sender);
	public void userLeave(SblockUser sender);
	
	public void setNick(String nick, SblockUser sender);
	public void removeNick(SblockUser sender);
	public CanonNicks getNick(SblockUser sender);
	
	public void setOwner(String name, SblockUser sender);
	public String getOwner();
	public void loadMod(String user);
	public void addMod(SblockUser user, SblockUser sender);
	public void removeMod(SblockUser user, SblockUser sender);
	public List<String>	getModList();
	public boolean isMod(SblockUser user);
	
	public void kickUser(SblockUser user, SblockUser sender);
	public void loadBan(String user);
	public void banUser(SblockUser user, SblockUser sender);
	public void unbanUser(SblockUser user, SblockUser sender);
	public boolean isBanned(SblockUser user);
	public void loadApproval(String user);
	public void approveUser(SblockUser user, SblockUser sender);
	public void deapproveUser(SblockUser user, SblockUser sender);
	public List<String> getApprovedUsers();
	
	public void disband(SblockUser sender);
	
	public void sendToAll(SblockUser sender, String message);
	public List<String> getBanList();
	public boolean isOwner(SblockUser user);
	public boolean isChannelMod(SblockUser user);
	public boolean isApproved(SblockUser user);
}
