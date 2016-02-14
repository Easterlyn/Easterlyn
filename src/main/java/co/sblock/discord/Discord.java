package co.sblock.discord;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import org.reflections.Reflections;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.discord.commands.DiscordCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.module.Module;
import co.sblock.users.Users;
import co.sblock.utilities.PlayerLoader;
import co.sblock.utilities.TextUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.HTTP429Exception;

/**
 * A Module for managing messaging to and from Discord.
 * 
 * @author Jikoo
 */
public class Discord extends Module {

	private final String chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private final Pattern toEscape = Pattern.compile("([\\_~*])"),
			spaceword = Pattern.compile("(\\s*)(\\S*)"), mention = Pattern.compile("<@(\\d+)>");
	private final ConcurrentLinkedQueue<Triple<String, String, String>> queue;
	private final Map<String, DiscordCommand> commands;
	private final LoadingCache<Object, Object> authentications;
	private final YamlConfiguration discordData;
	private final Optional<String> login, password;

	private IDiscordClient client;
	private Users users;
	private ChannelManager manager;
	private MessageBuilder builder;
	private BukkitTask postTask;

	public Discord(Sblock plugin) {
		super(plugin);

		queue = new ConcurrentLinkedQueue<>();
		commands = new HashMap<>();

		authentications = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build(
				new CacheLoader<Object, Object>() {
					@Override
					public Object load(Object key) throws Exception {
						if (!(key instanceof UUID)) {
							return null;
						}
						String code = generateUniqueCode();
						authentications.put(code, key);
						return code;
					};
				});

		File file = new File(plugin.getDataFolder(), "DiscordData.yml");
		if (file.exists()) {
			discordData = YamlConfiguration.loadConfiguration(file);
		} else {
			discordData = new YamlConfiguration();
		}

		login = Optional.of(getConfig().getString("discord.login"));
		password = Optional.of(getConfig().getString("discord.password"));

		Reflections reflections = new Reflections("co.sblock.discord.commands");
		Set<Class<? extends DiscordCommand>> cmds = reflections.getSubTypesOf(DiscordCommand.class);
		for (Class<? extends DiscordCommand> command : cmds) {
			if (Modifier.isAbstract(command.getModifiers())) {
				continue;
			}
			if (!Sblock.areDependenciesPresent(command)) {
				getLogger().warning(command.getSimpleName() + " is missing dependencies, skipping.");
				continue;
			}
			try {
				Constructor<? extends DiscordCommand> constructor = command.getConstructor(getClass());
				DiscordCommand cmd = constructor.newInstance(this);
				commands.put('/' + cmd.getName(), cmd);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onEnable() {
		if (login == null || password == null) {
			getLogger().severe("Unable to connect to Discord, no username or password!");
			this.disable();
			return;
		}

		this.users = getPlugin().getModule(Users.class);

		try {
			this.client = new ClientBuilder().withLogin(this.login.get(), this.password.get()).build();
			EventDispatcher dispatcher = this.client.getDispatcher();
			dispatcher.registerListener(new DiscordReadyListener(this));

			this.client.login();

			dispatcher.registerListener(new DiscordMessageReceivedListener(this));
		} catch (DiscordException e) {
			e.printStackTrace();
			this.disable();
			return;
		}

		this.manager = getPlugin().getModule(Chat.class).getChannelManager();
		// future modify MessageBuilder to allow custom name clicks (OPEN_URL www.sblock.co/discord)
		this.builder = new MessageBuilder(getPlugin()).setNameClick("@# ").setChannelClick("@# ")
				.setChannel(this.manager.getChannel("#discord"))
				.setNameHover(TextComponent.fromLegacyText(Color.GOOD_EMPHASIS + "Discord Chat\n"
						+ ChatColor.BLUE + ChatColor.UNDERLINE + "www.sblock.co/discord\n"
						+ Color.GOOD + "Channel: #main"));
	}

	private String generateUniqueCode() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			sb.append(chars.charAt(getPlugin().getRandom().nextInt(chars.length())));
		}
		String code = sb.toString();
		if (authentications.getIfPresent(code) != null) {
			return generateUniqueCode();
		}
		return code;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.client != null;
	}

	protected void startPostingMessages() {
		if (postTask != null && postTask.getTaskId() != -1) {
			return;
		}
		postTask = new BukkitRunnable() {
			@Override
			public void run() {
				if (!isEnabled()) {
					cancel();
					return;
				}
				if (queue.isEmpty()) {
					return;
				}
				Triple<String, String, String> triple = queue.poll();
				IChannel group = client.getChannelByID(triple.getLeft());
				if (group == null) {
					IUser user = client.getUserByID(triple.getLeft());
					if (user == null) {
						return;
					}
					try {
						group = client.getOrCreatePMChannel(user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					client.changeAccountInfo(Optional.of(triple.getMiddle()), login, password,
							Optional.empty());
				} catch (HTTP429Exception | DiscordException e) {
					// Trivial issue
				}
				try {
					group.sendMessage(triple.getRight());
				} catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(getPlugin(), 20L, 20L);
	}

	public IDiscordClient getAPI() {
		return this.client;
	}

	public void logMessage(String message) {
		postMessage("Sbot", message, getConfig().getString("discord.chat.log"));
	}

	public void postMessage(Message message, boolean global) {
		if (global) {
			postMessage(message.getSenderName(), message.getDiscordMessage(),
					getConfig().getString("discord.chat.main"));
		}
		postMessage(message.getSenderName(), message.getConsoleMessage(),
				getConfig().getString("discord.chat.log"));
	}

	public void postMessage(String name, String message, boolean global) {
		if (global) {
			postMessage(name, message, getConfig().getString("discord.chat.main"),
					getConfig().getString("discord.chat.log"));
		} else {
			postMessage(name, message, getConfig().getString("discord.chat.log"));
		}
	}

	public void postMessage(String name, String message, String... channels) {
		if (!isEnabled()) {
			return;
		}
		// TODO allow formatting codes in any chat? Could support markdown rather than &codes.
		name = ChatColor.stripColor(toEscape.matcher(name).replaceAll("\\\\$1"));
		message = ChatColor.stripColor(message);
		if (message.trim().isEmpty()) {
			return;
		}
		// Discord is case-sensitive. This prevents an @everyone alert without altering content.
		message = message.replace("@everyone", "@Everyone");
		StringBuilder builder = new StringBuilder();
		Matcher matcher = spaceword.matcher(message);
		while (matcher.find()) {
			builder.append(matcher.group(1));
			String word = matcher.group(2);
			if (!TextUtils.URL_PATTERN.matcher(word).find()) {
				word = toEscape.matcher(word).replaceAll("\\\\$1");
			}
			builder.append(word);
		}
		for (String channel : channels) {
			queue.add(new ImmutableTriple<>(channel, name, builder.toString()));
		}
	}

	public void postReport(String name, String message) {
		postMessage(name, message, getConfig().getString("discord.chat.reports"));
	}

	public LoadingCache<Object, Object> getAuthCodes() {
		return authentications;
	}

	@Override
	protected void onDisable() {
		if (client != null) {
			try {
				client.logout();
			} catch (HTTP429Exception | DiscordException e) {
				e.printStackTrace();
			}
		}
		try {
			discordData.save(new File(getPlugin().getDataFolder(), "DiscordData.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "Discord";
	}

	public UUID getUUIDOf(IUser user) {
		String uuidString = discordData.getString("users." + user.getID());
		if (uuidString == null) {
			return null;
		}
		return UUID.fromString(uuidString);
	}

	public void addLink(UUID uuid, IUser user) {
		discordData.set("users." + user.getID(), uuid.toString());
	}

	protected DiscordPlayer getPlayerFor(IUser user) {
		UUID uuid = getUUIDOf(user);
		if (uuid == null) {
			return null;
		}
		Player player = PlayerLoader.getPlayer(uuid);
		if (player instanceof DiscordPlayer) {
			return (DiscordPlayer) player;
		}
		// PlayerLoader loads a PermissiblePlayer, wrapping a wrapper would be silly.
		DiscordPlayer dplayer = new DiscordPlayer(this, user, player.getPlayer());
		PlayerLoader.modifyCachedPlayer(dplayer);
		return dplayer;
	}

	protected boolean handleDiscordCommandFor(String command, IUser user, IChannel channel) {
		String[] split = command.split("\\s");
		if (!commands.containsKey(split[0])) {
			return false;
		}
		String[] args = new String[split.length - 1];
		if (args.length > 0) {
			System.arraycopy(split, 1, args, 0, args.length);
		}
		commands.get(split[0]).execute(user, channel, args);
		return true;
	}

	protected void handleMinecraftCommandFor(DiscordPlayer player, String command, IChannel channel) {
		if (player.hasPendingCommand()) {
			postMessage("Sbot", "You already have a pending command. Please be patient.", channel.getID());
			return;
		}
		Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(getPlugin(),
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						player.startMessages();
						PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
						Bukkit.getPluginManager().callEvent(event);
						return !event.isCancelled() && Bukkit.dispatchCommand(player, event.getMessage().substring(1));
					}
				});

		new BukkitRunnable() {
			@Override
			public void run() {
				int count = 0;
				while (!future.isDone() && count < 20) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						future.cancel(false);
						break;
					}
					count++;
				}
				if (future.isCancelled() || !future.isDone()) {
					postMessage("Sbot", "Command " + command + " from " + player.getName() + " timed out.", channel.getID());
					player.stopMessages();
					return;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) { }
				String message = player.stopMessages();
				if (message.isEmpty()) {
					return;
				}
				postMessage("Sbot", message, channel.getID());
			}
		}.runTaskAsynchronously(getPlugin());
	}

	protected void handleChatToMinecraft(IMessage message, Player player) {
		String content = message.getContent();
		if (!player.hasPermission("sblock.discord.filterexempt")) {
			int newline = content.indexOf('\n');
			boolean delete = false;
			if (newline > 0) {
				postMessage("Sbot", "Newlines are not allowed in messages to Minecraft, <@"
						+ message.getAuthor().getID() + ">", message.getChannel().getID());
				delete = true;
			} else if (content.length() > 255) {
				postMessage("Sbot", "Messages from Discord may not be over 255 characters, <@"
						+ message.getAuthor().getID() + ">", message.getChannel().getID());
				delete = true;
			}
			if (delete) {
				try {
					message.delete();
				} catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
					// Trivial
				}
			}
		}
		builder.setSender(users.getUser(player.getUniqueId()))
				.setMessage(sanitize(content)).setChannel(manager.getChannel("#discord"));
		if (!builder.canBuild(false)) {
			try {
				message.delete();
			} catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
				// Trivial
			}
			return;
		}
		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> users.getUser(p.getUniqueId()).getSuppression());
		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(true, player, players, builder.toMessage()));
	}

	private String sanitize(String message) {
		Matcher matcher = mention.matcher(message);
		StringBuilder sb = new StringBuilder();
		int lastMatch = 0;
		while (matcher.find()) {
			sb.append(message.substring(lastMatch, matcher.start())).append('@');
			String id = matcher.group(1);
			IUser user = client.getUserByID(id);
			if (user == null) {
				sb.append(id);
			} else {
				sb.append(client.getUserByID(id).getName());
			}
			lastMatch = matcher.end();
		}
		sb.append(message.substring(lastMatch));
		return sb.toString();
	}

}
