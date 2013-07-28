package co.sblock.Sblock.Chat.Channel;

import java.util.HashMap;
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

	public void createNewChannel(String name, AccessLevel sendingAccess,
			AccessLevel listeningAccess, String creator) {
		Channel c = new NormalChannel(name, sendingAccess, listeningAccess,
				creator);
		ChannelManager.getChannelList().put(name, c);
		Logger.getLogger("Minecraft").info("Channel" + c.getName()
				+ "created: " + sendingAccess + " "
						+ listeningAccess + " " + creator);
	}

	public void createDefaultChannel() {
		Channel c = new NormalChannel("#", AccessLevel.PUBLIC,
				AccessLevel.PUBLIC, "Dublek");
		Logger.getLogger("Minecraft").info("Default chat channel created");
		ChannelManager.getChannelList().put("#", c);
	}

	public void dropChannel(String channelname) {
		ChannelManager.getChannelList().remove(channelname);
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
