package co.sblock.Sblock.Chat.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import co.sblock.Sblock.DatabaseManager;


public class ChannelManager {
	//Writing this part from scratch
	//Here be dragons...
	//wait maybe this all needs to be static. ADAMMM
	
	//Should be the interface between plugin and database. Do all loads and saves as infrequently as possible
	
	private static Map<String, Channel> channelList = new HashMap<String, Channel>();
	
	public ChannelManager()	{
	}
	
	public void loadAllChannels()	{
		//SELECT * FROM ChatChannels
		//foreach entry
		//new Channel(column info goes here)
	}
	public void loadChannels()	{
		DatabaseManager.getDatabaseManager().loadAllChannelData();
//		Channel c = new NormalChannel(name, sendingAccess, listeningAccess, creator);
//		ChannelManager.getChannelList().put(name, c);


	}
	public void saveAllChannels()	{
		//foreach channel
		//UPDATE blahblahblah		
	}
	public void saveChannel(String channelName)	{
		//do the savey thing, john!
	}
	
	public void createNewChannel(String name, AccessLevel sendingAccess, AccessLevel listeningAccess, String creator)	{
		//INSERT INTO ChatChannels
		//VALUES (values go here, from parameters)
		//channelList.put(name, channel);
		Channel c = new NormalChannel(name, sendingAccess, listeningAccess, creator);
		ChannelManager.getChannelList().put(name, c);
		Logger.getLogger("Minecraft").info("Channel" + c.getName() + "created: " + sendingAccess + " " + listeningAccess + " " + creator);
	}
	public void createDefaultChannel()	{
		Channel c = new NormalChannel("#", AccessLevel.PUBLIC, AccessLevel.PUBLIC, "Dublek");
		Logger.getLogger("Minecraft").info("Default chat channel created");
		ChannelManager.getChannelList().put("#", c);
	}
	public void dropChannel(String channelname)	{
		ChannelManager.getChannelList().remove(channelname);
		//DROP row?
	}
	public static Map<String, Channel> getChannelList()	{
		return channelList;
	}
	public Channel getChannel(String channelname)	{
		if(channelList.containsKey(channelname))	{
			return channelList.get(channelname);
		}
		else	{
			return null;
		}
	}
	public boolean isValidChannel(String channelname)	{
		return channelList.containsValue(channelname);
	}
	
	//...fuck. did I just write channelmanager in the usermanager? GODDAMNIT! 1 JOB
	//fixed!
}
