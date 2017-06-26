package com.easterlyn.chat;

import com.easterlyn.chat.channel.AccessLevel;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.ChannelType;
import com.easterlyn.chat.channel.NickChannel;
import com.easterlyn.chat.channel.NormalChannel;
import com.easterlyn.chat.channel.RegionChannel;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {

	private final Chat chat;
	private final ConcurrentHashMap<String, Channel> channelList = new ConcurrentHashMap<>();

	protected ChannelManager(Chat chat) {
		this.chat = chat;
	}

	void loadAllChannels() {
		final YamlConfiguration yaml = chat.getConfig();
		final ArrayList<String> drop = new ArrayList<>();
		for (String channelName : yaml.getKeys(false)) {
			NormalChannel channel;
			try {
				channel = chat.getChannelManager().loadChannel(channelName,
						AccessLevel.valueOf(yaml.getString(channelName + ".access")),
						UUID.fromString(yaml.getString(channelName + ".owner")),
						ChannelType.valueOf(yaml.getString(channelName + ".type")),
						yaml.getLong(channelName + ".lastAccess", 0));
			} catch (Exception e) {
				// Broken/invalid channel
				continue;
			}
			if (!channel.isRecentlyAccessed()) {
				drop.add(channelName);
				continue;
			}
			yaml.getStringList(channelName + ".mods").forEach(uuid -> channel.addModerator(UUID.fromString(uuid)));
			yaml.getStringList(channelName + ".bans").forEach(uuid -> channel.addBan(UUID.fromString(uuid)));
			yaml.getStringList(channelName + ".approved").forEach(uuid -> channel.addApproved(UUID.fromString(uuid)));
		}
		for (String channelName : drop) {
			yaml.set(channelName, null);
			this.channelList.remove(channelName);
		}
		if (drop.size() > 0) {
			this.chat.saveConfig();
		}

		this.createDefaultChannels();
	}

	void saveAllChannels() {
		for (Channel channel : this.channelList.values()) {
			if (channel instanceof NormalChannel && channel.getOwner() != null) {
				saveChannel((NormalChannel) channel);
			}
		}
		this.chat.saveConfig();
	}

	private void saveChannel(NormalChannel channel) {
		final YamlConfiguration yaml = this.chat.getConfig();
		final String name = channel.getName();
		yaml.set(name + ".owner", channel.getOwner().toString());
		yaml.set(name + ".type", channel.getClass().getSimpleName().replace("Channel", "").toUpperCase());
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
		yaml.set(name + ".lastAccess", channel.getLastAccess());
	}

	public void createNewChannel(String name, AccessLevel access, UUID creator, ChannelType channelType) {
		NormalChannel channel = this.loadChannel(name, access, creator, channelType, System.currentTimeMillis());
		this.saveChannel(channel);
		this.chat.saveConfig();
		this.chat.getLogger().info("Channel " + name + " created: " + access + " " + creator);
	}

	private NormalChannel loadChannel(String name, AccessLevel access, UUID creator, ChannelType channelType, long lastAccessed) {
		NormalChannel channel;
		switch (channelType) {
		case NICK:
			channel = new NickChannel(chat.getPlugin(), name, access, creator, lastAccessed);
			break;
		case NORMAL:
		default:
			channel = new NormalChannel(chat.getPlugin(), name, access, creator, lastAccessed);
			break;
		}
		this.channelList.put(name, channel);
		return channel;
	}

	private void createDefaultChannels() {
		Channel main = new RegionChannel(chat.getPlugin(), "#");
		this.channelList.put("#", main);
		this.channelList.put("#main", main);
		this.channelList.put("#Aether", new RegionChannel(chat.getPlugin(), "#Aether"));
		this.channelList.put("#discord", new RegionChannel(chat.getPlugin(), "#discord"));
		String spam = chat.getPlugin().getModule(Language.class).getValue("chat.spamChannel");
		this.channelList.put(spam, new NormalChannel(chat.getPlugin(), spam, AccessLevel.PUBLIC, null, Long.MAX_VALUE));
		// People may use unicode characters in private messages
		this.channelList.put("#pm", new NickChannel(chat.getPlugin(), "#pm", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6"), Long.MAX_VALUE));
		// Show true sign contents
		this.channelList.put("#sign", new NickChannel(chat.getPlugin(), "#sign", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6"), Long.MAX_VALUE));
		// Tests should be done as-is, no filters
		this.channelList.put("@test@", new NickChannel(chat.getPlugin(), "@test@", AccessLevel.PRIVATE, UUID.fromString("40028b1a-b4d7-4feb-8f66-3b82511ecdd6"), Long.MAX_VALUE));
		this.channelList.put("@", new NormalChannel(chat.getPlugin(), "@", AccessLevel.PRIVATE, null, Long.MAX_VALUE));
	}

	public void dropChannel(String channelName) {
		this.channelList.remove(channelName);
		this.chat.getConfig().set(channelName, null);
		this.chat.saveConfig();
	}

	public Map<String, Channel> getChannelList() {
		return this.channelList;
	}

	public Channel getChannel(String channelname) {
		if (channelname == null) {
			return null;
		}
		if (!this.channelList.containsKey(channelname)) {
			// Ignore case when matching.
			for (Entry<String, Channel> entry : this.channelList.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(channelname)) {
					return entry.getValue();
				}
			}
			return null;
		}
		return this.channelList.get(channelname);
	}

}
