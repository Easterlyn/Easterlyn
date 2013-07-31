package co.sblock.Sblock.Chat.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import co.sblock.Sblock.DatabaseManager;

public class ChannelManager {

	private static Map<String, Channel> channelList = new HashMap<String, Channel>();

	public void loadAllChannels() {
		DatabaseManager.getDatabaseManager().loadAllChannelData();
	}

	public void saveAllChannels() {
		for (Channel c : channelList.values()) {
			DatabaseManager.getDatabaseManager().saveChannelData(c);
		}
	}

	public void saveChannel(Channel c) {
		DatabaseManager.getDatabaseManager().saveChannelData(c);
	}

	public void createNewChannel(String name, AccessLevel access, String creator) {
		Channel c = new NormalChannel(name, access, creator);
		ChannelManager.getChannelList().put(name, c);
		Logger.getLogger("Minecraft").info("Channel" + c.getName() + "created: " + access + " " + creator);
	}

	public void createDefaultSet() {
		List<Channel> defaults = new ArrayList<Channel>();
		defaults.add(new NormalChannel("#", AccessLevel.PUBLIC, "Dublek"));
/*		defaults.add(new RPChannel("#rp", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RPChannel("#rp2", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#earth", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#InnerCircle", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#OuterCircle", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#FurthestRing", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOWAS", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOLAR", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOHAC", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOFAF", AccessLevel.PUBLIC, "Dublek"));
		*/
		
		Logger.getLogger("Minecraft").info("Default channels created");
		for(Channel c : defaults)	{
			ChannelManager.getChannelList().put("#", c);
		}
	}

	public void dropChannel(String channelName) {
		ChannelManager.getChannelList().remove(channelName);
		DatabaseManager.getDatabaseManager().deleteChannel(channelName);
	}

	public static Map<String, Channel> getChannelList() {
		return channelList;
	}

	public Channel getChannel(String channelname) {
		if (channelList.containsKey(channelname)) {
			return channelList.get(channelname);
		} else {
			return null;
		}
	}

	public boolean isValidChannel(String channelname) {
		return channelList.containsValue(channelname);
	}
}
