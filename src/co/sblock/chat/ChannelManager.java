package co.sblock.chat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.NormalChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.data.SblockData;
import co.sblock.users.User;


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
		this.channelList.put(name, c);
	}

	public void createDefaultSet() {
		channelList.put("#", new RegionChannel("#", AccessLevel.PUBLIC, null));
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

	public Map<String, Channel> getChannelList() {
		return this.channelList;
	}

	public Channel getChannel(String channelname) {
		if (channelname == null || !channelList.containsKey(channelname)) {
			// ConcurrentHashMap tends to NPE instead of returning null. Manual fix!
			return null;
		}
		return channelList.get(channelname);
	}

	public boolean isValidChannel(String channelname) {
		return channelList.containsKey(channelname);
	}

	public Message parseMessage(Player player, String message) {
		User sender = User.getUser(player.getUniqueId());
		Channel destination;
		int space = message.indexOf(' ');
		// Check for @<channel> destination
		if (message.charAt(0) == '@' && space > 1) {
			String target = message.substring(1, space);
			message = message.substring(space);
			destination = this.getChannel(target);
			if (destination == null) {
				sender.sendMessage(ChatMsgs.errorInvalidChannel(target));
			}
		} else  {
			destination = sender.getCurrent();
			if (destination == null) {
				sender.sendMessage(ChatMsgs.errorNoCurrent());
			}
		}
		return new Message(sender, destination, message);
	}

	public static ChannelManager getChannelManager() {
		return SblockChat.getChat().getChannelManager();
	}
}
