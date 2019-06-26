package com.easterlyn.chat;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import com.easterlyn.Easterlyn;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NormalChannel;
import com.easterlyn.chat.channel.SecretChannel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.users.User;
import com.easterlyn.users.UserRank;
import com.easterlyn.util.Colors;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.event.SimpleListener;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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

	private static final Pattern CHANNEL_PATTERN = Pattern.compile("^(#[A-Za-z0-9]{0,15})([\\W&&[^" + ChatColor.COLOR_CHAR + "}])?$");
	public static final Channel DEFAULT = new Channel("main", UUID.fromString("902b498d-9909-4e78-b401-b7c4f2b1ab4c"));
	public static final String USER_CHANNELS = "chat.channels";
	public static final String USER_CURRENT = "chat.current";
	public static final String USER_HIGHLIGHTS = "chat.highlights";

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
		PermissionUtil.addParent("easterlyn.chat.channel.owner", UserRank.ADMIN.getPermission());
		// Permission to make funky channel names
		PermissionUtil.addParent("easterlyn.command.channel.create.anyname", UserRank.HEAD_ADMIN.getPermission());

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
				}, this));

		channels.put("", DEFAULT);
		channels.put("main", DEFAULT);
		channels.put("aether", DEFAULT);
		channels.put("discord", DEFAULT);
		channels.put("pm", new SecretChannel("pm", DEFAULT.getOwner()));
		channels.put("sign", new SecretChannel("sign", DEFAULT.getOwner()));
		channels.put("#", new SecretChannel("#", DEFAULT.getOwner()));
		channels.put("@test@", new SecretChannel("@test@", DEFAULT.getOwner()));

		// TODO rich messages
		AsyncPlayerChatEvent.getHandlerList().register(new SimpleListener<>(AsyncPlayerChatEvent.class, event -> {
			RegisteredServiceProvider<Easterlyn> easterlynRSP = getServer().getServicesManager().getRegistration(Easterlyn.class);
			if (easterlynRSP == null) {
				event.getPlayer().sendMessage("Easterlyn core plugin is not enabled! Please report this to @Staff on Discord immediately!");
				return;
			}

			event.setCancelled(true);

			User user = easterlynRSP.getProvider().getUserManager().getUser(event.getPlayer().getUniqueId());

			Channel channel = null;

			// #channel message parsing
			if (event.getMessage().length() > 0 && event.getMessage().charAt(0) == '#') {
				int space = event.getMessage().indexOf(' ');
				if (space == -1) {
					space = event.getMessage().length();
				}
				String channelName = event.getMessage().substring(1, space);
				if (space == event.getMessage().length()) {
					user.sendMessage("What are you trying to say in #" + channelName + "?");
					return;
				}
				channel = getChannels().get(channelName);
				if (channel == null) {
					user.sendMessage("Invalid channel. Create it with `/channel create #" + channelName + "`!");
					return;
				}
				event.setMessage(event.getMessage().substring(space));
			}

			// User's channel
			if (channel == null) {
				channel = getChannels().get(user.getStorage().getString(EasterlynChat.USER_CURRENT));
				if (channel == null) {
					user.sendMessage("No current channel set! Focus on the main channel with `/join #`!");
					return;
				}
			}

			new UserChatEvent(user, channel, event.getMessage()).send();
		}, this, EventPriority.MONITOR, true));

		// TODO anti-spam section
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
		StringUtil.addSectionHandler(string -> new StringUtil.SingleMatcher(CHANNEL_PATTERN.matcher(string)) {
			@Override
			protected TextComponent[] handleMatch(TextComponent previousComponent) {
				String word = getMatcher().group();
				TextComponent component = new TextComponent();
				String channelString = getMatcher().group(1);
				int end = getMatcher().end(1);
				component.setText(word.substring(0, end));
				component.setColor(Colors.CHANNEL);
				component.setUnderlined(true);
				component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						TextComponent.fromLegacyText(Colors.COMMAND + "/join " + Colors.CHANNEL + channelString)));
				component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + channelString));

				String group = getMatcher().group(2);
				if (group == null || group.isEmpty()) {
					return new TextComponent[] {component};
				}

				TextComponent suffix = new TextComponent(previousComponent);
				suffix.setText(group);

				return new TextComponent[] {component, suffix};
			}
		});

		plugin.getCommandManager().registerDependency(this.getClass(), this);

		IssuerAwareContextResolver<Channel, BukkitCommandExecutionContext> channelContext = context -> {
			if (context.hasFlag("self")) {
				if (!context.getIssuer().isPlayer()) {
					throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
				}
				String channelName = plugin.getUserManager().getUser(context.getIssuer().getUniqueId()).getStorage().getString(USER_CURRENT);
				if (channelName == null || !channels.containsKey(channelName)) {
					throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, "{message}", "No current channel set!");
				}
				return channels.get(channelName);
			}
			String channelName = context.getFirstArg();
			if (channelName.length() == 0 || channelName.charAt(0) != '#') {
				if (context.hasFlag("other")) {
					throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, "{message}", "No channel specified!");
				}
			}
			channelName = channelName.substring(1);
			Channel channel = channels.get(channelName);
			if (channel != null) {
				context.popFirstArg();
				return channel;
			}
			if (context.hasFlag("other")) {
				throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, "{message}", "No channel specified!");
			}
			channelName = plugin.getUserManager().getUser(context.getIssuer().getUniqueId()).getStorage().getString(USER_CURRENT);
			if (channelName == null || !channels.containsKey(channelName)) {
				throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, "{message}", "No current channel set!");
			}
			return channels.get(channelName);
		};

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(Channel.class, channelContext);
		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(NormalChannel.class, context -> {
			Channel channel = channelContext.getContext(context);
			if (!(channel instanceof NormalChannel)) {
				throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, "{message}", "Channel is not a normal channel!");
			}
			return (NormalChannel) channel;
		});

		// TODO completions: Channel, NormalChannel
		plugin.registerCommands("com.easterlyn.chat.commands");
	}

}
