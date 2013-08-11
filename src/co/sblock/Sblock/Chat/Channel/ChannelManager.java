package co.sblock.Sblock.Chat.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Utilities.Sblogger;

public class ChannelManager {

	private static Map<String, Channel> channelList = new HashMap<String, Channel>();

	public void loadAllChannels() {
		DatabaseManager.getDatabaseManager().loadAllChannelData();
	}

	public void saveAllChannels() {
		for (Channel c : channelList.values()) {
			if (!c.getType().equals(ChannelType.TEMP)
					&& !c.getType().equals(ChannelType.REGION))
				DatabaseManager.getDatabaseManager().saveChannelData(c);
		}
	}

	public void saveChannel(Channel c) {
		DatabaseManager.getDatabaseManager().saveChannelData(c);
	}

	public void createNewChannel(String name, AccessLevel access, String creator, ChannelType channelType) {
		Channel c = new NormalChannel(name, access, creator);
		ChannelManager.getChannelList().put(name, c);
		ChatModule.slog().info("Channel" + c.getName() + "created: " + access + " " + creator);
	}

	public void createDefaultSet() {
		List<Channel> defaults = new ArrayList<Channel>();
		defaults.add(new NormalChannel("#", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RPChannel("#rp", AccessLevel.PUBLIC, "Dublek"));
/*		defaults.add(new RPChannel("#rp2", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#Earth", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#InnerCircle", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#OuterCircle", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#FurthestRing", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOWAS", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOLAR", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOHAC", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOFAF", AccessLevel.PUBLIC, "Dublek"));*/
		
		
		Sblogger.info("SblockChat", "Default channels created");
		for(Channel c : defaults)	{
			ChannelManager.getChannelList().put(c.getName(), c);
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
		return channelList.containsKey(channelname);
	}

	public static ChannelManager getChannelManager() {
		return ChatModule.getChatModule().getChannelManager();
	}
}
