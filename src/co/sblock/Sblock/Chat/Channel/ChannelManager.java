package co.sblock.Sblock.Chat.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.sblock.Sblock.Chat.SblockChat;
import co.sblock.Sblock.Database.SblockData;

public class ChannelManager {

	private static Map<String, Channel> channelList = new HashMap<String, Channel>();

	private List<String> noSave = new ArrayList<String>();

	public void loadAllChannels() {
		SblockData.getDB().loadAllChannelData();
	}

	public void saveAllChannels() {
		for (Channel c : channelList.values()) {
			if (!(noSave.contains(c.getName()) || c instanceof RegionChannel
				/*	|| c instanceof TempChannel*/))
				SblockData.getDB().saveChannelData(c);
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
		List<Channel> defaults = new ArrayList<Channel>();
		defaults.add(new NormalChannel("#", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new NormalChannel("#help", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RPChannel("#rp", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RPChannel("#rp2", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RegionChannel("#EARTH", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RegionChannel("#INNERCIRCLE", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RegionChannel("#OUTERCIRCLE", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RegionChannel("#FURTHESTRING", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RegionChannel("#LOWAS", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RegionChannel("#LOLAR", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RegionChannel("#LOHAC", AccessLevel.PUBLIC, UUID.fromString("Dublek")));
		defaults.add(new RegionChannel("#LOFAF", AccessLevel.PUBLIC, UUID.fromString("Dublek")));

		for (Channel c : defaults) {
			ChannelManager.getChannelList().put(c.getName(), c);
			this.addUnsavableChannel(c.getName());
		}
	}

	private void addUnsavableChannel(String s) {
		noSave.add(s);
	}

	public void dropChannel(String channelName) {
		ChannelManager.getChannelList().remove(channelName);
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
