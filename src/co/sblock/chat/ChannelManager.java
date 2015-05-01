package co.sblock.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import co.sblock.Sblock;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.NormalChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.chat.channel.RegionChannel;

public class ChannelManager {

	private final ConcurrentHashMap<String, Channel> channelList = new ConcurrentHashMap<>();

	public void loadAllChannels() {
		final File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "ChatChannels.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load channel data!", e);
		}
		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		for (String channelName : yaml.getKeys(false)) {
			final Channel channel = ChannelManager.getChannelManager().loadChannel(channelName,
					AccessLevel.valueOf(yaml.getString(channelName + ".access")),
					UUID.fromString(yaml.getString(channelName + ".owner")),
					ChannelType.valueOf(yaml.getString(channelName + ".type")));
			yaml.getStringList(channelName + ".mods").forEach(uuid -> channel.addModerator(UUID.fromString(uuid)));
			yaml.getStringList(channelName + ".bans").forEach(uuid -> channel.addBan(UUID.fromString(uuid)));
			yaml.getStringList(channelName + ".approved").forEach(uuid -> channel.addApproved(UUID.fromString(uuid)));
		}
	}

	public void saveAllChannels() {
		final File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "ChatChannels.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to save all channel data!", e);
		}
		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		for (Channel channel : channelList.values()) {
			if (channel.getOwner() == null) {
				// Default channel
				continue;
			}
			final String name = channel.getName();
			yaml.set(name + ".owner", channel.getOwner().toString());
			yaml.set(name + ".type", channel.getType().name());
			yaml.set(name + ".access", channel.getAccess().name());
			final ArrayList<String> mods = new ArrayList<>();
			channel.getModList().forEach(uuid -> mods.add(uuid.toString()));
			yaml.set(name + ".mods", mods);
			final ArrayList<String> bans = new ArrayList<>();
			channel.getBanList().forEach(uuid -> bans.add(uuid.toString()));
			yaml.set(name + ".bans", bans);
			final ArrayList<String> approved = new ArrayList<>();
			channel.getApprovedUsers().forEach(uuid -> approved.add(uuid.toString()));
			yaml.set(name + ".approved", approved);
		}
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save all channel data!", e);
		}
	}

	public void saveChannel(Channel channel) {
		final File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "ChatChannels.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for channel " + channel.getName(), e);
		}
		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		final String name = channel.getName();
		yaml.set(name + ".owner", channel.getOwner().toString());
		yaml.set(name + ".type", channel.getType().name());
		yaml.set(name + ".access", channel.getAccess().name());
		final ArrayList<String> mods = new ArrayList<>();
		channel.getModList().forEach(uuid -> mods.add(uuid.toString()));
		yaml.set(name + ".mods", mods);
		final ArrayList<String> bans = new ArrayList<>();
		channel.getBanList().forEach(uuid -> bans.add(uuid.toString()));
		yaml.set(name + ".bans", bans);
		final ArrayList<String> approved = new ArrayList<>();
		channel.getApprovedUsers().forEach(uuid -> approved.add(uuid.toString()));
		yaml.set(name + ".approved", approved);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for channel " + channel.getName(), e);
		}
	}

	public void createNewChannel(String name, AccessLevel access, UUID creator, ChannelType channelType) {
		this.loadChannel(name, access, creator, channelType);
		Chat.getChat().getLogger().info("Channel " + name + " created: " + access + " " + creator);
	}

	private Channel loadChannel(String name, AccessLevel access, UUID creator, ChannelType channelType) {
		Channel channel;
		switch (channelType) {
		case NICK:
			channel = new NickChannel(name, access, creator);
			break;
		case REGION:
			channel = new RegionChannel(name, access, creator);
			break;
		case RP:
			channel = new RPChannel(name, access, creator);
			break;
		case NORMAL:
		default:
			channel = new NormalChannel(name, access, creator);
			break;
		}
		this.channelList.put(name, channel);
		return channel;
	}

	public void createDefaultSet() {
		channelList.put("#", new RegionChannel("#", AccessLevel.PUBLIC, null));
		channelList.put("#help", new NormalChannel("#help", AccessLevel.PUBLIC, null));
		channelList.put("#rp", new RPChannel("#rp", AccessLevel.PUBLIC, null));
		channelList.put("#fanrp", new NickChannel("#fanrp", AccessLevel.PUBLIC, null));
		channelList.put("#EARTH", new RegionChannel("#EARTH", AccessLevel.PUBLIC, null));
		channelList.put("#DERSPIT", new RegionChannel("#DERSPIT", AccessLevel.PUBLIC, null));
		channelList.put("#INNERCIRCLE", new RegionChannel("#INNERCIRCLE", AccessLevel.PUBLIC, null));
		channelList.put("#OUTERCIRCLE", new RegionChannel("#OUTERCIRCLE", AccessLevel.PUBLIC, null));
		channelList.put("#FURTHESTRING", new RegionChannel("#FURTHESTRING", AccessLevel.PUBLIC, null));
		channelList.put("#LOWAS", new RegionChannel("#LOWAS", AccessLevel.PUBLIC, null));
		channelList.put("#LOLAR", new RegionChannel("#LOLAR", AccessLevel.PUBLIC, null));
		channelList.put("#LOHAC", new RegionChannel("#LOHAC", AccessLevel.PUBLIC, null));
		channelList.put("#LOFAF", new RegionChannel("#LOFAF", AccessLevel.PUBLIC, null));
		channelList.put("#Aether", new RegionChannel("#Aether", AccessLevel.PUBLIC, null));
		channelList.put("#halchat", new NormalChannel("#halchat", AccessLevel.PUBLIC, null));
		channelList.put("#gods", new NormalChannel("#gods", AccessLevel.PUBLIC, null));
		channelList.put("#pm", new NickChannel("#pm", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6")));
		channelList.put("@", new NormalChannel("@", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6")));
	}

	public void dropChannel(String channelName) {
		channelList.remove(channelName);
		File file;
		file = new File(Sblock.getInstance().getDataFolder(), "ChatChannels.yml");
		if (!file.exists()) {
			return;
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		yaml.set(channelName, null);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to delete channel " + channelName, e);
		}
	}

	public Map<String, Channel> getChannelList() {
		return this.channelList;
	}

	public Channel getChannel(String channelname) {
		if (channelname == null) {
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

	public static ChannelManager getChannelManager() {
		return Chat.getChat().getChannelManager();
	}
}
