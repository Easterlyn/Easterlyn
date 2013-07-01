package co.sblock.Sblock.Chat;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import co.sblock.Sblock.Chat.Channel.Channel;

public class User {
	
	private Player pthis;
	private Channel current;
	private boolean isMute;
	
	private static ArrayList<User> userList = new ArrayList<User>();
	
	public User (Player p)	{
		this.pthis = p;
		//this.current = somehow based on current region
		this.isMute = false;
		userList.add(this);
		//Channel?.joinChannelFirstTime(Region);
	}
	
	public static void addPlayer (Player p)	{
		//if player is new (check PlayerData table), call constructor
		//else load data from db
		
		//if (pg.SELECT*FROMPlayerDataWHEREplayerName=p)		
		//then we're all cool
		//else new User(p);
		
		
	}
	public static void removePlayer (Player p)	{
		
	}
	public static void getUser (Player p)	{
		
	}

}
