package co.sblock.Sblock.Chat.Channel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;

import co.sblock.Sblock.Chat.User;

public class NormalChannel implements Channel	{

	protected String name;
	protected String alias;
	protected AccessLevel listenAccess;
	protected AccessLevel sendAccess;
	protected String owner;
	
	protected List<String> modList = new ArrayList<String>();
	protected List<String> muteList = new ArrayList<String>();
	protected transient Set<User> listening = new HashSet<User>();
	
	public NormalChannel(String name, AccessLevel sendingAccess, AccessLevel listeningAccess, String creator)	{
		this.name = name;
		this.alias = null;
		this.sendAccess = sendingAccess;
		this.listenAccess = listeningAccess;
		this.owner = creator;
		this.modList.add(creator);
		
		//also, INSERT INTO all this stuff into the main ChatChannels table in the db
		//also CREATE TABLE channelname and add owner as first record. This table for all listeners
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getJoinChatMessage(User sender) {
		String time24h = new SimpleDateFormat("HH:mm").format(new Date());
		return ChatColor.DARK_GREEN + sender.getName() + ChatColor.YELLOW + " began pestering " + ChatColor.GOLD 
				+ this.name + ChatColor.YELLOW + " at " + time24h;
	}

	@Override
	public String getLeaveChatMessage(User sender) {
		 return this.getJoinChatMessage(sender).replaceAll("began", "ceased");
	}

	@Override
	public AccessLevel getSAcess() {
		return this.sendAccess;
	}

	@Override
	public AccessLevel getLAcess() {
		return this.listenAccess;
	}

	@Override
	public Set<User> getUsers() {
		// TODO Figure out how/when/where to load this from the channel table in db
		return this.listening;
	}

	@Override
	public ChannelType getType() {
		return ChannelType.NORMAL;
	}

	@Override
	public void addAlias(String name, User sender) {
		//if sender = mod (or owner)
		//this.alias = name
		//else "you need to be a channel mod to do this!"
		
	}

	@Override
	public void removeAlias(String name, User sender) {
		//same ifelse as addAlias()
		//this.alias = null;
		
	}

	@Override
	public boolean userJoin(User sender) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void userLeave(User sender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChat(String message, User sender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNick(String nick, User sender) {
		//"this channel does not permit nicknames"
		
	}

	@Override
	public void removeNick(User sender) {
		//"this channel does not permit nicknames"
	}

	@Override
	public void setOwner(String newO, User sender) {
		if(sender.equals(this.owner))	{
			this.owner = newO;
		}		
	}

	@Override
	public void addMod(User user, User sender) {
		//SburbChat code. Handle with care
		/*
		if (sender.getName().equals(owner) && !modList.contains(user.getName()))
		{
			this.modList.add(user.getName());
			this.sendToAll(ChatColor.YELLOW + user.getName() + " is now a mod in " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!", sender);
			user.sendMessage(ChatColor.GREEN + "You are now a mod in " + ChatColor.GOLD + this.name + ChatColor.GREEN + "!");
		}
		else if (!sender.getName().equals(owner))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to mod people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is already a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		*/
	}

	@Override
	public void addMod(String user) {
		this.modList.add(user);
	}

	@Override
	public void removeMod(User user, User sender) {
		//SburbChat code. Handle with care
		/*
		 if (sender.getName().equals(this.owner) && this.modList.contains(user.getName()))
		{
			this.modList.remove(user.getName());
			this.sendToAll(ChatColor.YELLOW + user.getName() + " is no longer a mod in " + ChatColor.GOLD + this.name + ChatColor.YELLOW + "!", sender);
			user.sendMessage(ChatColor.RED + "You are no longer a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else if (!sender.getName().equals(this.owner))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to demod people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is not a mod in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}		 
		 */
	}

	@Override
	public void kickUser(User user, User sender) {
		//SburbChat code. Handle with care
		/*
		if (modList.contains(sender.getName()) && listening.contains(user))
		{
			this.listening.remove(user);
			user.kickFrom(this);
			this.sendToAll(ChatColor.YELLOW + user.getName() + " has been kicked from " + ChatColor.GOLD + this.getName() + ChatColor.YELLOW + "!", sender);
		}
		else if (!modList.contains(sender.getName()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to kick people in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.RED + " is not chatting in " + ChatColor.GOLD + this.name + ChatColor.RED + "!");
		}
		 */
		
	}

	@Override
	public void banUser(User user, User sender) {
		// TODO Auto-generated method stub
		//Currently no way to store banned users. A list in a db that supports such things would be exquisite
	}

	@Override
	public void unbanUser(User user, User sender) {
		// TODO Auto-generated method stub
		//same issue as in banUser()		
	}

	@Override
	public void muteUser(User user, User sender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unmuteUser(User user, User sender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void approveUser(User user, User sender) {
		// TODO Auto-generated method stub
		//DEPRECATED, no request channels in this version
	}

	@Override
	public void deapproveUser(User user, User sender) {
		// TODO Auto-generated method stub
		//DEPRECATED, no request channels in this version
	}

	@Override
	public void disband(User sender) {
		// TODO Auto-generated method stub
		//Prolly copy Ben's code here again		
	}
	protected void sendToAll(String s, User sender) {
		for (User u : this.listening)
		{
			u.sendMessageFromChannel(s, this);
		}
		//Logger.getLogger("Minecraft").info(ChatColor.stripColor(this.getPrefix(sender) + s));
		//TODO fix the above log message with the new output sequence
	}

}
