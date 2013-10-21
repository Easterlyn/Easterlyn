package co.sblock.Sblock.Chat.Channel;

import java.util.Set;

import co.sblock.Sblock.Chat.ChatUser;

public interface Channel {
	public String getName();
	public AccessLevel getAccess();
	public Set<String> getListening();
	public ChannelType getType();
	
	public void addListening(String user);
	public void removeListening(String user);
	
	public void setNick(ChatUser sender, String nick);
	public void removeNick(ChatUser sender);
	public String getNick(ChatUser u);
	public boolean hasNick(ChatUser u);
	
	public void setOwner(String name, ChatUser sender);
	public String getOwner();
	public void loadMod(String user);
	public void addMod(String username, ChatUser sender);
	public void removeMod(String target, ChatUser sender);
	public Set<String> getModList();
	public boolean isMod(ChatUser user);
	
	public void kickUser(ChatUser chatUser, ChatUser sender);
	public void loadBan(String user);
	public void banUser(String username, ChatUser sender);
	public void unbanUser(String username, ChatUser sender);
	public boolean isBanned(ChatUser user);
	public void loadApproval(String user);
	public void approveUser(ChatUser user, ChatUser sender);
	public void deapproveUser(ChatUser user, ChatUser sender);
	public Set<String> getApprovedUsers();
	
	public void disband(ChatUser sender);
	
	public void sendToAll(ChatUser sender, String message, String type);
	public Set<String> getBanList();
	public boolean isOwner(ChatUser user);
	public boolean isChannelMod(ChatUser user);
	public boolean isApproved(ChatUser user);

}
