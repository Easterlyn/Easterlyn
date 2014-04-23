package co.sblock.chat.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.sblock.chat.SblockChat;
import co.sblock.data.SblockData;


public class ChannelManager {

	private static Map<String, Channel> channelList = new HashMap<String, Channel>();

	public void loadAllChannels() {
		SblockData.getDB().loadAllChannelData();
	}

	public void saveAllChannels() {
		for (Channel c : channelList.values()) {
			if (c.getOwner() != null) {
				SblockData.getDB().saveChannelData(c);
			}
		}
	}

	public void saveChannel(Channel c) {
		SblockData.getDB().saveChannelData(c);
	}

	public void createNewChannel(String name, AccessLevel access, UUID creator, ChannelType channelType) {
		this.loadChannel(name, access, creator, channelType);
		SblockChat.getChat().getLogger().info("Channel " + name + " created: " + access + " " + creator);
	}

	public void loadChannel(String name, AccessLevel access, UUID creator, ChannelType channelType) {
		Channel c = null;
		switch (channelType) {
		case RP:
			c = new RPChannel(name, access, creator);
			break;
		case NICK:
			c = new NickChannel(name, access, creator);
			break;
		case REGION:
			c = new RegionChannel(name, access, creator);
			break;
		default:
			c = new NormalChannel(name, access, creator);
			break;
		}
		ChannelManager.getChannelList().put(name, c);
	}

	public void createDefaultSet() {
		channelList.put("#", new NormalChannel("#", AccessLevel.PUBLIC, null));
		channelList.put("#help", new NormalChannel("#help", AccessLevel.PUBLIC, null));
		channelList.put("#rp", new RPChannel("#rp", AccessLevel.PUBLIC, null));
		channelList.put("#rp2", new RPChannel("#rp2", AccessLevel.PUBLIC, null));
		channelList.put("#EARTH", new RegionChannel("#EARTH", AccessLevel.PUBLIC, null));
		channelList.put("#INNERCIRCLE", new RegionChannel("#INNERCIRCLE", AccessLevel.PUBLIC, null));
		channelList.put("#OUTERCIRCLE", new RegionChannel("#OUTERCIRCLE", AccessLevel.PUBLIC, null));
		channelList.put("#FURTHESTRING", new RegionChannel("#FURTHESTRING", AccessLevel.PUBLIC, null));
		channelList.put("#LOWAS", new RegionChannel("#LOWAS", AccessLevel.PUBLIC, null));
		channelList.put("#LOLAR", new RegionChannel("#LOLAR", AccessLevel.PUBLIC, null));
		channelList.put("#LOHAC", new RegionChannel("#LOHAC", AccessLevel.PUBLIC, null));
		channelList.put("#LOFAF", new RegionChannel("#LOFAF", AccessLevel.PUBLIC, null));

	}

	public void dropChannel(String channelName) {
		channelList.remove(channelName);
		SblockData.getDB().deleteChannel(channelName);
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
		return SblockChat.getChat().getChannelManager();
	}
}
