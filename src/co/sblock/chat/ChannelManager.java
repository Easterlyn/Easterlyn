package co.sblock.chat;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NormalChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.data.SblockData;
import co.sblock.chat.channel.Channel.ChannelSerialiser;


public class ChannelManager {

	private ConcurrentHashMap<String, Channel> channelList;

	public ChannelManager() {
		channelList = new ConcurrentHashMap<>();
	}

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
		Channel c = new ChannelSerialiser(channelType, name, access, creator).build();
		this.loadChannel(name, c);
		SblockChat.getChat().getLogger().info("Channel " + name + " created: " + access + " " + creator);
	}

	public void loadChannel(String name, Channel c) {
		this.channelList.put(name, c);
	}

	public void createDefaultSet() {
		channelList.put("#", new RegionChannel("#", AccessLevel.PUBLIC, null));
		channelList.put("#help", new NormalChannel("#help", AccessLevel.PUBLIC, null));
		channelList.put("#rp", new RPChannel("#rp", AccessLevel.PUBLIC, null));
		channelList.put("#rp2", new RPChannel("#rp2", AccessLevel.PUBLIC, null));
		channelList.put("#EARTH", new RegionChannel("#EARTH", AccessLevel.PUBLIC, null));
		channelList.put("#DERSPIT", new RegionChannel("#DERSPIT", AccessLevel.PUBLIC, null));
		channelList.put("#INNERCIRCLE", new RegionChannel("#INNERCIRCLE", AccessLevel.PUBLIC, null));
		channelList.put("#OUTERCIRCLE", new RegionChannel("#OUTERCIRCLE", AccessLevel.PUBLIC, null));
		channelList.put("#FURTHESTRING", new RegionChannel("#FURTHESTRING", AccessLevel.PUBLIC, null));
		channelList.put("#LOWAS", new RegionChannel("#LOWAS", AccessLevel.PUBLIC, null));
		channelList.put("#LOLAR", new RegionChannel("#LOLAR", AccessLevel.PUBLIC, null));
		channelList.put("#LOHAC", new RegionChannel("#LOHAC", AccessLevel.PUBLIC, null));
		channelList.put("#LOFAF", new RegionChannel("#LOFAF", AccessLevel.PUBLIC, null));
		channelList.put("@", new NormalChannel("@", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6")));
	}

	public void dropChannel(String channelName) {
		channelList.remove(channelName);
		SblockData.getDB().deleteChannel(channelName);
	}

	public Map<String, Channel> getChannelList() {
		return this.channelList;
	}

	public Channel getChannel(String channelname) {
		if (channelname == null) {
			// ConcurrentHashMap tends to NPE instead of returning null. Manual fix!
			return null;
		}
		if (!channelList.containsKey(channelname)) {
			// Ignore case when matching.
			for (Entry<String, Channel> entry : channelList.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(channelname)) {
					return entry.getValue();
				}
			}
			return null;
		}
		return channelList.get(channelname);
	}

	public boolean isValidChannel(String channelname) {
		return channelList.containsKey(channelname);
	}

	public static ChannelManager getChannelManager() {
		return SblockChat.getChat().getChannelManager();
	}
}
