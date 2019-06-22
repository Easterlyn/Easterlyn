package com.easterlyn.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.SecretChannel;
import com.easterlyn.users.UserRank;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.event.SimpleListener;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Plugin for managing Easterlyn's chat system.
 *
 * @author Jikoo
 */
public class EasterlynChat extends JavaPlugin {

	public static final Channel DEFAULT = new Channel("main", UUID.fromString("902b498d-9909-4e78-b401-b7c4f2b1ab4c"));
	public static final String USER_CHANNELS = "chat.channels";
	public static final String USER_CURRENT = "chat.current";

	private final Map<String, Channel> channels = new ConcurrentHashMap<>();

	@Override
	public void onEnable() {
		RegisteredServiceProvider<Easterlyn> registration = getServer().getServicesManager().getRegistration(Easterlyn.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		// Permission to use >greentext.
		PermissionUtil.getOrCreate("easterlyn.chat.greentext", PermissionDefault.TRUE);
		// Permission to bypass all chat filtering.
		PermissionUtil.addParent("easterlyn.chat.spam", UserRank.MODERATOR.getPermission());
		PermissionUtil.addParent("easterlyn.chat.spam", "easterlyn.spam");
		// Permission to use all caps.
		PermissionUtil.getOrCreate("easterlyn.chat.spam.caps", PermissionDefault.TRUE);
		PermissionUtil.addParent("easterlyn.chat.spam.caps", "easterlyn.chat.spam");
		// Permission to use non-ascii characters and mixed case words.
		PermissionUtil.getOrCreate("easterlyn.chat.spam.normalize", PermissionDefault.TRUE);
		PermissionUtil.addParent("easterlyn.chat.spam.normalize", "easterlyn.chat.spam");
		// Permission to not be affected by speed limitations.
		PermissionUtil.getOrCreate("easterlyn.chat.spam.fast", PermissionDefault.TRUE);
		PermissionUtil.addParent("easterlyn.chat.spam.fast", "easterlyn.chat.spam");
		// Permission for gibberish filtering, average characters per word.
		PermissionUtil.addParent("easterlyn.chat.spam.gibberish", "easterlyn.chat.spam");
		// Permission to send duplicate messages in a row within 30 seconds.
		// Default false - quite handy to prevent accidental uparrow enter.
		PermissionUtil.getOrCreate("easterlyn.chat.spam.repeat", PermissionDefault.FALSE);
		// Permission for messages to automatically color using name color.
		PermissionUtil.getOrCreate("easterlyn.chat.color", PermissionDefault.FALSE);
		// Permission to be recognized as a moderator in every channel.
		PermissionUtil.addParent("easterlyn.chat.channel.moderator", UserRank.STAFF.getPermission());
		// Permission to be recognized as an owner in every channel.
		PermissionUtil.addParent("easterlyn.chat.channel.owner", UserRank.MODERATOR.getPermission());

		FileConfiguration config = getConfig();
		Set<String> remove = new HashSet<>();
		config.getKeys(false).forEach(key -> {
			if (!loadChannel(key, config.getConfigurationSection(key))) {
				remove.add(key);
			}
		});

		remove.forEach(key -> config.set(key, null));

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class,
				pluginEnableEvent -> {
					if (pluginEnableEvent.getPlugin() instanceof Easterlyn) {
						register((Easterlyn) pluginEnableEvent.getPlugin());
					}
				}, EventPriority.NORMAL, this, true));

		channels.put("", DEFAULT);
		channels.put("main", DEFAULT);
		channels.put("aether", DEFAULT);
		channels.put("discord", DEFAULT);
		channels.put("pm", new SecretChannel("pm", DEFAULT.getOwner()));
		channels.put("sign", new SecretChannel("sign", DEFAULT.getOwner()));
		channels.put("#", new SecretChannel("#", DEFAULT.getOwner()));
		channels.put("@test@", new SecretChannel("@test@", DEFAULT.getOwner()));

	}

	public Map<String, Channel> getChannels() {
		return channels;
	}

	@Override
	public void onDisable() {
		FileConfiguration config = getConfig();
		channels.values().forEach(channel -> {
			if (channel.isRecentlyAccessed()) {
				channel.save(config);
			} else {
				config.set(channel.getName(), null);
			}
		});
	}

	private boolean loadChannel(@NotNull String name, @Nullable ConfigurationSection data) {
		if (data == null) {
			return false;
		}

		String className = data.getString("class");
		if (className == null) {
			return false;
		}

		Class<?> channelClass;
		try {
			channelClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			return false;
		}
		if (!Channel.class.isAssignableFrom(channelClass)) {
			return false;
		}

		Constructor<?> channelConstructor;
		try {
			channelConstructor = channelClass.getConstructor(String.class, UUID.class);
		} catch (NoSuchMethodException e) {
			return false;
		}

		String uuidString = data.getString("owner");
		if (uuidString == null) {
			return false;
		}
		UUID owner;
		try {
			owner = UUID.fromString(uuidString);
		} catch (Exception e) {
			return false;
		}

		Channel channel;
		try {
			channel = (Channel) channelConstructor.newInstance(name, owner);
		} catch (ReflectiveOperationException e) {
			return false;
		}

		if (!channel.isRecentlyAccessed()) {
			return false;
		}

		channel.load(data);

		channels.put(name, channel);
		return true;

	}

	private void register(Easterlyn plugin) {
		plugin.getCommandManager().registerDependency(this.getClass(), this);
		// TODO completions: Channel, NormalChannel
		plugin.registerCommands("com.easterlyn.chat.commands");
	}

}
