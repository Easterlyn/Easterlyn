package co.sblock.Sblock.Chat.Channel;

import java.util.Set;

import co.sblock.Sblock.UserData.SblockUser;

public interface Channel {
	public String getName();
	public AccessLevel getAccess();
	public Set<String> getListening();
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
	public Set<String> getModList();
	public boolean isMod(SblockUser user);
	
	public void kickUser(SblockUser user, SblockUser sender);
	public void loadBan(String user);
	public void banUser(SblockUser user, SblockUser sender);
	public void unbanUser(SblockUser user, SblockUser sender);
	public boolean isBanned(SblockUser user);
	public void loadApproval(String user);
	public void approveUser(SblockUser user, SblockUser sender);
	public void deapproveUser(SblockUser user, SblockUser sender);
	public Set<String> getApprovedUsers();
	
	public void disband(SblockUser sender);
	
	public void sendToAll(SblockUser sender, String message, String type);
	public Set<String> getBanList();
	public boolean isOwner(SblockUser user);
	public boolean isChannelMod(SblockUser user);
	public boolean isApproved(SblockUser user);
}
