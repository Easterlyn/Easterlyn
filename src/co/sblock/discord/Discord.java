package co.sblock.discord;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import co.sblock.utilities.RegexUtils;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.DiscordBuilder;
import me.itsghost.jdiscord.events.UserChatEvent;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

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
	private DiscordAPI discord;
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
		String login = getConfig().getString("discord.login");
		String password = getConfig().getString("discord.password");

		if (login == null || password == null) {
			getLogger().severe("Unable to connect to Discord, no username or password!");
			this.disable();
			return;
		}

		discord = new DiscordBuilder(login, password).build();

		this.users = getPlugin().getModule(Users.class);

		discord.getEventManager().registerListener(new DiscordLoadedListener(this));

		try {
			discord.login();
		} catch (NoLoginDetailsException | BadUsernamePasswordException | DiscordFailedToConnectException e) {
			e.printStackTrace();
			this.disable();
			return;
		}

		manager = getPlugin().getModule(Chat.class).getChannelManager();
		// future modify MessageBuilder to allow custom name clicks (OPEN_URL www.sblock.co/discord)
		builder = new MessageBuilder(getPlugin()).setNameClick("@# ").setChannelClick("@# ")
				.setChannel(manager.getChannel("#discord"))
				.setNameHover(TextComponent.fromLegacyText(Color.GOOD_EMPHASIS + "Discord Chat\n"
						+ ChatColor.BLUE + ChatColor.UNDERLINE + "www.sblock.co/discord\n"
						+ Color.GOOD + "Channel: #main"));
		discord.getEventManager().registerListener(new DiscordChatListener(this));
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
		return super.isEnabled() && this.discord != null;
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
				Group group = discord.getGroupById(triple.getLeft());
				if (group == null) {
					return;
				}
				discord.getSelfInfo().setUsername(triple.getMiddle());
				group.sendMessage(triple.getRight());
			}
		}.runTaskTimerAsynchronously(getPlugin(), 20L, 20L);
	}

	public DiscordAPI getAPI() {
		return this.discord;
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
		name = ChatColor.stripColor(name);
		message = ChatColor.stripColor(message);
		if (message.trim().isEmpty()) {
			return;
		}
		StringBuilder builder = new StringBuilder();
		Matcher matcher = spaceword.matcher(message);
		while (matcher.find()) {
			builder.append(matcher.group(1));
			String word = matcher.group(2);
			if (!RegexUtils.URL_PATTERN.matcher(word).find()) {
				word = toEscape.matcher(word).replaceAll("\\\\$1");
			}
			builder.append(word);
		}
		for (String channel : channels) {
			queue.add(new ImmutableTriple<>(channel, name, message));
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
		if (discord != null) {
			discord.stop();
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

	public UUID getUUIDOf(User user) {
		String uuidString = discordData.getString("users." + user.getId());
		if (uuidString == null) {
			return null;
		}
		return UUID.fromString(uuidString);
	}

	public void addLink(UUID uuid, User user) {
		discordData.set("users." + user.getId(), uuid.toString());
	}

	protected DiscordPlayer getPlayerFor(GroupUser user) {
		UUID uuid = getUUIDOf(user.getUser());
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

	protected boolean handleDiscordCommandFor(String command, GroupUser user, Group group) {
		String[] split = command.split("\\s");
		if (!commands.containsKey(split[0])) {
			return false;
		}
		String[] args = new String[split.length - 1];
		if (args.length > 0) {
			System.arraycopy(split, 1, args, 0, args.length);
		}
		commands.get(split[0]).execute(user, group, args);
		return true;
	}

	protected void handleMinecraftCommandFor(DiscordPlayer player, String command, Group group) {
		if (player.hasPendingCommand()) {
			postMessage("Sbot", "You already have a pending command. Please be patient.", group.getId());
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
					postMessage("Sbot", "Command " + command + " from " + player.getName() + " timed out.", group.getId());
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
				postMessage("Sbot", message, group.getId());
			}
		}.runTaskAsynchronously(getPlugin());
	}

	protected void handleChatToMinecraft(UserChatEvent event, Player player) {
		String message = event.getMsg().getMessage();
		if (!player.hasPermission("sblock.discord.filterexempt")) {
			int newline = message.indexOf('\n');
			if (newline > 0) {
				postMessage("Sbot", "Newlines are not allowed in messages to Minecraft, <@"
						+ event.getUser().getUser().getId() + ">", event.getGroup().getId());
				return;
			}
			if (message.length() > 255) {
				postMessage("Sbot", "Messages from Discord may not be over 255 characters, <@"
						+ event.getUser().getUser().getId() + ">", event.getGroup().getId());
			}
		}
		builder.setSender(users.getUser(player.getUniqueId()))
				.setMessage(sanitize(message)).setChannel(manager.getChannel("#discord"));
		if (!builder.canBuild(false)) {
			event.getMsg().deleteMessage();
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
			User user = discord.getUserById(id);
			if (user == null) {
				sb.append(id);
			} else {
				sb.append(discord.getUserById(id).getUsername());
			}
			lastMatch = matcher.end();
		}
		sb.append(message.substring(lastMatch));
		return sb.toString();
	}

}
