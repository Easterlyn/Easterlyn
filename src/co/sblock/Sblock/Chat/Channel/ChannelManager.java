package co.sblock.Sblock.Chat.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.Chat.Channel.NormalChannel;
import co.sblock.Sblock.Chat.Channel.RegionChannel;
import co.sblock.Sblock.Utilities.Sblogger;

public class ChannelManager {

	private static Map<String, Channel> channelList = new HashMap<String, Channel>();

	private List<String> noSave = new ArrayList<String>();

	public void loadAllChannels() {
		DatabaseManager.getDatabaseManager().loadAllChannelData();
	}

	public void saveAllChannels() {
		for (Channel c : channelList.values()) {
			if (!(noSave.contains(c.getName()) || c instanceof RegionChannel
				/*	|| c instanceof TempChannel*/))
				DatabaseManager.getDatabaseManager().saveChannelData(c);
		}
	}

	public void saveChannel(Channel c) {
		DatabaseManager.getDatabaseManager().saveChannelData(c);
	}

	public void createNewChannel(String name, AccessLevel access, String creator, ChannelType channelType) {
		Channel c = null;
		switch (channelType)	{
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
		ChatModule.slog().info("Channel " + c.getName() + " created: " + access + " " + creator);
	}

	public void createDefaultSet() {
		List<Channel> defaults = new ArrayList<Channel>();
		defaults.add(new NormalChannel("#", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new NormalChannel("#help", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RPChannel("#rp", AccessLevel.PUBLIC, "Dublek"));
//		defaults.add(new RPChannel("#rp2", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#EARTH", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#INNERCIRCLE", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#OUTERCIRCLE", AccessLevel.PUBLIC, "Dublek"));
/*		defaults.add(new RegionChannel("#FURTHESTRING", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOWAS", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOLAR", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOHAC", AccessLevel.PUBLIC, "Dublek"));
		defaults.add(new RegionChannel("#LOFAF", AccessLevel.PUBLIC, "Dublek"));*/

		for (Channel c : defaults) {
			ChannelManager.getChannelList().put(c.getName(), c);
			this.addUnsavableChannel(c.getName());
		}

		Sblogger.info("SblockChat", "Default channels created");
	}

	private void addUnsavableChannel(String s) {
		noSave.add(s);
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
