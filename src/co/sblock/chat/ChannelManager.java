package co.sblock.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.NormalChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.chat.channel.RegionChannel;

public class ChannelManager {

	private final Chat chat;
	private final ConcurrentHashMap<String, Channel> channelList = new ConcurrentHashMap<>();

	protected ChannelManager(Chat chat) {
		this.chat = chat;
	}

	protected void loadAllChannels() {
		final File file;
		try {
			file = new File(chat.getPlugin().getDataFolder(), "ChatChannels.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load channel data!", e);
		}
		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		final ArrayList<String> drop = new ArrayList<>();
		for (String channelName : yaml.getKeys(false)) {
			final Channel channel = chat.getChannelManager().loadChannel(channelName,
					AccessLevel.valueOf(yaml.getString(channelName + ".access")),
					UUID.fromString(yaml.getString(channelName + ".owner")),
					ChannelType.valueOf(yaml.getString(channelName + ".type")),
					yaml.getLong(channelName + ".lastAccess", System.currentTimeMillis()));
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
				chat.getLogger().warning("Unable to save when dropping old channels!");
				e.printStackTrace();
			}
		}
	}

	protected void saveAllChannels() {
		final File file;
		try {
			file = new File(chat.getPlugin().getDataFolder(), "ChatChannels.yml");
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

	protected void saveChannel(Channel channel) {
		if (!(channel instanceof NormalChannel)) {
			return;
		}
		NormalChannel normal = (NormalChannel) channel;
		final File file;
		try {
			file = new File(chat.getPlugin().getDataFolder(), "ChatChannels.yml");
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
		chat.getLogger().info("Channel " + name + " created: " + access + " " + creator);
	}

	private NormalChannel loadChannel(String name, AccessLevel access, UUID creator, ChannelType channelType, long lastAccessed) {
		NormalChannel channel;
		switch (channelType) {
		case NICK:
			channel = new NickChannel(chat.getPlugin(), name, access, creator, lastAccessed);
			break;
		case RP:
			channel = new RPChannel(chat.getPlugin(), name, access, creator, lastAccessed);
			break;
		case NORMAL:
		default:
			channel = new NormalChannel(chat.getPlugin(), name, access, creator, lastAccessed);
			break;
		}
		this.channelList.put(name, channel);
		return channel;
	}

	protected void createDefaultSet() {
		channelList.put("#", new RegionChannel(chat.getPlugin(), "#"));
		channelList.put("#Aether", new RegionChannel(chat.getPlugin(), "#Aether"));
		channelList.put("#discord", new RegionChannel(chat.getPlugin(), "#discord"));
		channelList.put("#rp", new RPChannel(chat.getPlugin(), "#rp", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		channelList.put("#fanrp", new NickChannel(chat.getPlugin(), "#fanrp", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		channelList.put("#halchat", new NormalChannel(chat.getPlugin(), "#halchat", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		channelList.put("#gods", new NormalChannel(chat.getPlugin(), "#gods", AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		// People may use unicode characters in private messages
		channelList.put("#pm", new NickChannel(chat.getPlugin(), "#pm", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6"), Long.MAX_VALUE));
		// Show true sign contents
		channelList.put("#sign", new NickChannel(chat.getPlugin(), "#sign", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6"), Long.MAX_VALUE));
		// Tests should be done as-is, no filters
		channelList.put("@test@", new NickChannel(chat.getPlugin(), "@test@", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6"), Long.MAX_VALUE));
		channelList.put("@", new NormalChannel(chat.getPlugin(), "@", AccessLevel.PRIVATE, null, Long.MAX_VALUE));
	}

	public void dropChannel(String channelName) {
		channelList.remove(channelName);
		File file;
		file = new File(chat.getPlugin().getDataFolder(), "ChatChannels.yml");
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
}
