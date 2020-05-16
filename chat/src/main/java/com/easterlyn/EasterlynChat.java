package com.easterlyn;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NormalChannel;
import com.easterlyn.chat.channel.SecretChannel;
import com.easterlyn.chat.listener.ChannelManagementListener;
import com.easterlyn.chat.listener.MuteListener;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.Colors;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.text.StaticQuoteConsumer;
import com.easterlyn.util.text.ParsedText;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
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

	private static final Pattern CHANNEL_PATTERN = Pattern.compile("^(#[A-Za-z0-9]{0,15})([\\W&&[^" + ChatColor.COLOR_CHAR + "}]])?$");
	public static final Channel DEFAULT = new Channel("main", UUID.fromString("902b498d-9909-4e78-b401-b7c4f2b1ab4c"));
	public static final String USER_CHANNELS = "chat.channels";
	public static final String USER_CURRENT = "chat.current";
	public static final String USER_HIGHLIGHTS = "chat.highlights";
	public static final String USER_MUTE = "chat.mute";

	private final Map<String, Channel> channels = new ConcurrentHashMap<>();

	@Override
	public void onEnable() {
		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class, event -> {
			if (event.getPlugin() instanceof EasterlynCore) {
				register((EasterlynCore) event.getPlugin());
			}
		}, this));

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

		channels.put("", DEFAULT);
		channels.put("main", DEFAULT);
		channels.put("aether", DEFAULT);
		channels.put("discord", DEFAULT);
		channels.put("pm", new SecretChannel("pm", DEFAULT.getOwner()));
		channels.put("sign", new SecretChannel("sign", DEFAULT.getOwner()));
		channels.put("#", new SecretChannel("#", DEFAULT.getOwner()));

		getServer().getPluginManager().registerEvents(new ChannelManagementListener(this), this);
		getServer().getPluginManager().registerEvents(new MuteListener(), this);

		// TODO
		//  - anti-spam listener
		//  - log signs to #sign

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

	private void register(EasterlynCore plugin) {
		StringUtil.addQuoteConsumer(new StaticQuoteConsumer(CHANNEL_PATTERN) {
			@Override
			public void addComponents(@NotNull ParsedText components, @NotNull Supplier<Matcher> matcherSupplier) {
				Matcher matcher = matcherSupplier.get();
				String word = matcher.group();
				TextComponent component = new TextComponent();
				String channelName = matcher.group(1);
				int end = matcher.end(1);
				component.setText(word.substring(0, end));
				component.setColor(Colors.CHANNEL.asBungee());
				component.setUnderlined(true);
				component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						TextComponent.fromLegacyText(Colors.COMMAND + "/join " + Colors.CHANNEL + channelName)));
				component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + channelName));
				components.addComponent(component);

				String trailingPunctuation = matcher.group(2);
				if (trailingPunctuation != null && !trailingPunctuation.isEmpty()) {
					components.addText(trailingPunctuation, component.getHoverEvent(), component.getClickEvent());
				}
			}
		});

		IssuerAwareContextResolver<Channel, BukkitCommandExecutionContext>
				channelContext = new IssuerAwareContextResolver<Channel, BukkitCommandExecutionContext>() {
			@Override
			public Channel getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
				//noinspection unchecked // Type erasure is caused by command context providing raw RegisteredCommand
				if (context.hasFlag(CoreContexts.SELF) || context.hasFlag(CoreContexts.ONLINE_WITH_PERM) && context.getIssuer().isPlayer()
						&& context.getCmd().getRequiredPermissions().stream().anyMatch(perm -> context.getIssuer().hasPermission(perm + ".other"))) {
					return getSelf(context.getIssuer());
				}

				if (context.hasFlag("TODO listening")) {
					return getOther(context.popFirstArg());
				}

				try {
					String firstArg = context.getFirstArg();
					Channel other = getOther(firstArg);
					context.popFirstArg();
					return other;
				} catch (InvalidCommandArgument ignored) {}

				return getSelf(context.getIssuer());
			}

			@NotNull
			Channel getSelf(@NotNull BukkitCommandIssuer issuer) throws InvalidCommandArgument {
				if (!issuer.isPlayer()) {
					throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
				}
				String channelName = plugin.getUserManager().getUser(issuer.getUniqueId()).getStorage().getString(USER_CURRENT);
				if (channelName == null || !channels.containsKey(channelName)) {
					throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, "{message}", "No current channel set!");
				}
				return channels.get(channelName);
			}

			@NotNull
			Channel getOther(@NotNull String argument) throws InvalidCommandArgument {
				if (argument.length() > 0 && argument.charAt(0) == '#') {
					argument = argument.substring(1).toLowerCase();
				} else {
					throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, "{message}", "Invalid channel format " + argument);
				}
				Channel channel = channels.get(argument);
				if (channel == null) {
					throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, "{message}", "Invalid channel #" + argument + "!");
				}
				return channel;
			}
		};

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(Channel.class, channelContext);
		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(NormalChannel.class, context -> {
			Channel channel = channelContext.getContext(context);
			if (!(channel instanceof NormalChannel)) {
				throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, false, "{message}", "Channel is not modifiable!");
			}
			return (NormalChannel) channel;
		});

		// TODO completions: Channel, NormalChannel
		plugin.registerCommands(this, getClassLoader(), "com.easterlyn.chat.command");
		plugin.getLocaleManager().addLocaleSupplier(this);
	}

}
