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
		final ArrayList<String> drop = new ArrayList<>();
		for (String channelName : yaml.getKeys(false)) {
			final Channel channel = ChannelManager.getChannelManager().loadChannel(channelName,
					AccessLevel.valueOf(yaml.getString(channelName + ".access")),
					UUID.fromString(yaml.getString(channelName + ".owner")),
					ChannelType.valueOf(yaml.getString(channelName + ".type")),
					yaml.getLong(channelName + ".lastAccessed", System.currentTimeMillis()));
			if (!(channel instanceof NormalChannel) || !((NormalChannel) channel).isRecentlyAccessed()) {
				drop.add(channelName);
				continue;
			}
			NormalChannel normal = (NormalChannel) channel;
			yaml.getStringList(channelName + ".mods").forEach(uuid -> normal.addModerator(UUID.fromString(uuid)));
			yaml.getStringList(channelName + ".bans").forEach(uuid -> normal.addBan(UUID.fromString(uuid)));
			yaml.getStringList(channelName + ".approved").forEach(uuid -> normal.addApproved(UUID.fromString(uuid)));
		}
		for (String channelName : drop) {
			yaml.set(channelName, null);
			channelList.remove(channelName);
		}
		if (drop.size() > 0) {
			try {
				yaml.save(file);
			} catch (IOException e) {
				Chat.getChat().getLogger().warning("Unable to save when dropping old channels!");
				e.printStackTrace();
			}
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
			if (channel.getOwner() == null || !(channel instanceof NormalChannel)) {
				// Default channel
				continue;
			}
			NormalChannel normal = (NormalChannel) channel;
			final String name = normal.getName();
			yaml.set(name + ".owner", normal.getOwner().toString());
			yaml.set(name + ".type", normal.getClass().getSimpleName().replace("Channel", "").toUpperCase());
			yaml.set(name + ".access", normal.getAccess().name());
			final ArrayList<String> mods = new ArrayList<>();
			normal.getModList().forEach(uuid -> mods.add(uuid.toString()));
			yaml.set(name + ".mods", mods);
			final ArrayList<String> bans = new ArrayList<>();
			normal.getBanList().forEach(uuid -> bans.add(uuid.toString()));
			yaml.set(name + ".bans", bans);
			final ArrayList<String> approved = new ArrayList<>();
			normal.getApprovedUsers().forEach(uuid -> approved.add(uuid.toString()));
			yaml.set(name + ".approved", approved);
			yaml.set(name + ".lastAccess", normal.getLastAccess());
		}
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save all channel data!", e);
		}
	}

	public void saveChannel(Channel channel) {
		if (!(channel instanceof NormalChannel)) {
			return;
		}
		NormalChannel normal = (NormalChannel) channel;
		final File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "ChatChannels.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for channel " + normal.getName(), e);
		}
		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		final String name = normal.getName();
		yaml.set(name + ".owner", normal.getOwner().toString());
		yaml.set(name + ".type", normal.getClass().getSimpleName().replace("Channel", "").toUpperCase());
		yaml.set(name + ".access", normal.getAccess().name());
		final ArrayList<String> mods = new ArrayList<>();
		normal.getModList().forEach(uuid -> mods.add(uuid.toString()));
		yaml.set(name + ".mods", mods);
		final ArrayList<String> bans = new ArrayList<>();
		normal.getBanList().forEach(uuid -> bans.add(uuid.toString()));
		yaml.set(name + ".bans", bans);
		final ArrayList<String> approved = new ArrayList<>();
		normal.getApprovedUsers().forEach(uuid -> approved.add(uuid.toString()));
		yaml.set(name + ".approved", approved);
		yaml.set(name + ".lastAccess", normal.getLastAccess());
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for channel " + normal.getName(), e);
		}
	}

	public void createNewChannel(String name, AccessLevel access, UUID creator, ChannelType channelType) {
		this.loadChannel(name, access, creator, channelType, System.currentTimeMillis());
		Chat.getChat().getLogger().info("Channel " + name + " created: " + access + " " + creator);
	}

	private NormalChannel loadChannel(String name, AccessLevel access, UUID creator, ChannelType channelType, long lastAccessed) {
		NormalChannel channel;
		switch (channelType) {
		case NICK:
			channel = new NickChannel(name, access, creator, lastAccessed);
			break;
		case RP:
			channel = new RPChannel(name, access, creator, lastAccessed);
			break;
		case NORMAL:
		default:
			channel = new NormalChannel(name, access, creator, lastAccessed);
			break;
		}
		this.channelList.put(name, channel);
		return channel;
	}

	public void createDefaultSet() {
		channelList.put("#", new RegionChannel("#"));
		channelList.put("#help", new NormalChannel("#help", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		channelList.put("#rp", new RPChannel("#rp", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		channelList.put("#fanrp", new NickChannel("#fanrp", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		channelList.put("#EARTH", new RegionChannel("#EARTH"));
		channelList.put("#DERSPIT", new RegionChannel("#DERSPIT"));
		channelList.put("#INNERCIRCLE", new RegionChannel("#INNERCIRCLE"));
		channelList.put("#OUTERCIRCLE", new RegionChannel("#OUTERCIRCLE"));
		channelList.put("#FURTHESTRING", new RegionChannel("#FURTHESTRING"));
		channelList.put("#LOWAS", new RegionChannel("#LOWAS"));
		channelList.put("#LOLAR", new RegionChannel("#LOLAR"));
		channelList.put("#LOHAC", new RegionChannel("#LOHAC"));
		channelList.put("#LOFAF", new RegionChannel("#LOFAF"));
		channelList.put("#Aether", new RegionChannel("#Aether"));
		channelList.put("#halchat", new NickChannel("#halchat", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		channelList.put("#gods", new NormalChannel("#gods", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		// #pm must have a real owner so that people may use unicode characters in private messages
		channelList.put("#pm", new NickChannel("#pm", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6"), Long.MAX_VALUE));
		channelList.put("@", new NormalChannel("@", AccessLevel.PRIVATE, null, Long.MAX_VALUE));
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
