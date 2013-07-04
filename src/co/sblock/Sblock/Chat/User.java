package co.sblock.Sblock.Chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.Sblock.Chat.Channel.Channel;

public class User {
	
	private Player pthis;
	private Channel current;
	private boolean isMute;
	private boolean isOnline;
	
	private Set<Channel> listening = new HashSet<Channel>();	//TODO: figure out how the hell to save this between runs
		//do I even want/need this?
	
	private static Map<String, User> userList = new HashMap<String, User>(); //TODO: same for this. halp
		
	public User(Player p)	{
		this.pthis = p;
		//this.current = somehow based on current region
		this.isMute = false;
		userList.put(pthis.getName(), this);
		//Channel?.joinChannelFirstTime(Region);
	}
	
	public static void addPlayer (Player p)	{ //Used for first-time logins
		new User(p);
	}
	public static User getUser (String name)	{
		return userList.get(name);
	}
	public String getUserName(String s)	{
		return this.pthis.getName();
	}
	public static void login(Player p)	{
		userList.get(p.getName()).isOnline = true;
	}
	public static void logout (Player p)	{
		userList.get(p.getName()).isOnline = false;
	}
	
	public void sendMessageFromChannel (String s, Channel c)	{
		
	}
	public void chat (AsyncPlayerChatEvent event)	{	//receives message from SblockChatListener
		
	}
	public String getName()	{
		return this.pthis.getName();
	}
	public String getDisplayName()	{	//colors for final display message are applied to $name here
		return null;
		
	}

}
