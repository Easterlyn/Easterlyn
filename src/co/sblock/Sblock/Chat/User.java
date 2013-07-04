package co.sblock.Sblock.Chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import co.sblock.Sblock.Chat.Channel.Channel;

public class User {
	
	private Player pthis;
	private Channel current;
	private boolean isMute;
	private boolean isOnline;
	
	private static Map<String, User> userList = new HashMap<String, User>();
		
	public User(Player p)	{
		this.pthis = p;
		//this.current = somehow based on current region
		this.isMute = false;
		userList.put(pthis.getName(), this);
		//Channel?.joinChannelFirstTime(Region);
	}
	
	public static void addPlayer (Player p)	{ //Used for first-time logins
		//if player is new (check PlayerData table), call constructor
		
		//if (pg.SELECT*FROMPlayerDataWHEREplayerName=p)		
		//then we're all cool
		//else new User(p);
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

}
