package co.sblock.discord;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.message.BasicNameValuePair;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import org.reflections.Reflections;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.gson.Gson;

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
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.Requests;

/**
 * A Module for managing messaging to and from Discord.
 * 
 * @author Jikoo
 */
public class Discord extends Module {

	public static final String BOT_NAME = "Sbot";

	private final String chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private final Pattern toEscape = Pattern.compile("([\\_~*])"),
			spaceword = Pattern.compile("(\\s*)(\\S*)"), mention = Pattern.compile("<@(\\d+)>");
	private final ConcurrentLinkedQueue<Triple<String, String, String>> queue;
	private final Map<String, DiscordCommand> commands;
	private final LoadingCache<Object, Object> authentications;
	private final YamlConfiguration discordData;
	private final Cache<IMessage, String> pastMainMessages;
	private final Map<String, IMessage> channelRetentionData;

	private String channelMain, channelLog, channelReports;
	private Optional<String> login, password;
	private IDiscordClient client;
	private Users users;
	private ChannelManager manager;
	private MessageBuilder builder;
	private BukkitTask heartbeatTask, postTask;

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

		pastMainMessages = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(20)
				.removalListener(new RemovalListener<IMessage, String>() {
					@Override
					public void onRemoval(RemovalNotification<IMessage, String> notification) {
						StringBuilder builder = new StringBuilder(notification.getValue());
						builder.append(": ").append(notification.getKey().getContent());
						try {
							notification.getKey().edit(builder.toString());
						} catch (HTTP429Exception | DiscordException e) {
							e.printStackTrace();
						} catch (MissingPermissionsException e) {
							// Silently fail, not our message somehow
						}
					}
				}).build();

		this.channelRetentionData = new HashMap<>();

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
		login = Optional.of(getConfig().getString("discord.login"));
		password = Optional.of(getConfig().getString("discord.password"));
		channelMain = getConfig().getString("discord.chat.main");
		channelLog = getConfig().getString("discord.chat.log");
		channelReports = getConfig().getString("discord.chat.reports");

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

	@Override
	protected void onDisable() {
		pastMainMessages.invalidateAll();
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
	public YamlConfiguration loadConfig() {
		super.loadConfig();

		login = Optional.of(getConfig().getString("discord.login"));
		password = Optional.of(getConfig().getString("discord.password"));
		channelMain = getConfig().getString("discord.chat.main");
		channelLog = getConfig().getString("discord.chat.log");
		channelReports = getConfig().getString("discord.chat.reports");

		return getConfig();
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

	protected void startHeartbeat() {
		if (heartbeatTask != null && heartbeatTask.getTaskId() != -1) {
			return;
		}
		heartbeatTask = new BukkitRunnable() {
			@Override
			public void run() {
				if (!isEnabled()) {
					cancel();
					return;
				}
				// In case no one is on or talking, clean up messages anyway
				pastMainMessages.cleanUp();

				if (!discordData.isConfigurationSection("retention")) {
					return;
				}
				ConfigurationSection retention = discordData.getConfigurationSection("retention");
				for (String guildID : retention.getKeys(false)) {
					if (!retention.isConfigurationSection(guildID)) {
						continue;
					}
					ConfigurationSection retentionGuild = retention.getConfigurationSection(guildID);
					for (String channelID : retentionGuild.getKeys(false)) {
						if (retentionGuild.isSet(channelID)) {
							doRetention(client.getChannelByID(channelID), retentionGuild.getLong(channelID, -1));
						}
					}
				}
			}
		}.runTaskTimerAsynchronously(getPlugin(), 0L, 12000L); // 10 minutes between checks
	}

	/**
	 * This method is blocking and will run until either the bot is rate limited or messages old
	 * enough have been deleted.
	 * 
	 * @param channel the IChannel to do retention for
	 * @param duration the duration in seconds
	 */
	private void doRetention(IChannel channel, long duration) {
		// This method requires touching a lot of Discord4J internals. TODO open a PR?
		if (duration == -1 || !(channel instanceof Channel)) {
			return;
		}

		// Ensure we can read history and delete messages
		try {
			if (!(channel instanceof IPrivateChannel) && !(channel instanceof IVoiceChannel)) {
				DiscordUtils.checkPermissions(client, channel, EnumSet.of(Permissions.READ_MESSAGE_HISTORY, Permissions.MANAGE_MESSAGES));
			}
		} catch (MissingPermissionsException e) {
			getLogger().warning("Unable to do retention for channel " + channel.mention() + " - cannot read history and delete messages!");
			return;
		}

		// Here, we get funky. Channel history must be populated.
		// To reduce our usage of Discord's API, we store the ID of the last retained message in channels with retention policies.

		// TODO Check if it's possible to look up a single message by ID

		List<IMessage> channelHistory = new ArrayList<>();
		LocalDateTime latestAllowed = LocalDateTime.now().minusSeconds(duration);
		IMessage earliestMessage = null;
		LocalDateTime earliestAcceptableTimestamp = null;
		// TODO reduce duplicate code, this is a shitshow
		if (!channelRetentionData.containsKey(channel.getID())) {
			boolean more = true;
			while (more) {
				Collection<IMessage> pastMessages;
				try {
					pastMessages = getPastMessages((Channel) channel, 50,
							earliestMessage == null ? null : earliestMessage.getID(), null);
				} catch (HTTP429Exception e1) {
					System.out.println("Rate limited, repeating request in a second.");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
					continue;
				}
				if (pastMessages.size() < 50) {
					more = false;
				}
				for (IMessage message : pastMessages) {
					LocalDateTime messageTime = message.getTimestamp();
					// Check if message was posted before our latest allowed timestamp.
					if (latestAllowed.isAfter(messageTime)) {
						channelHistory.add(message);
						continue;
					}
					// Check if earliest is after, aka earlier, but latest is before, aka still later.
					// If so, we've got a new latest allowed stamp.
					if ((earliestAcceptableTimestamp == null || earliestMessage.getTimestamp().isAfter(messageTime))
							&& latestAllowed.isBefore(messageTime)) {
						earliestMessage = message;
						continue;
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			channelRetentionData.put(channel.getID(), earliestMessage);
		} else {
			boolean more = true;
			while (more) {
				Collection<IMessage> pastMessages;
				try {
					pastMessages = getPastMessages((Channel) channel, 50, null,
							earliestMessage == null ? channelRetentionData.get(channel.getID()).getID() : earliestMessage.getID());
				} catch (HTTP429Exception e1) {
					System.out.println("Rate limited, repeating request in a second.");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
					continue;
				}
				if (pastMessages.size() < 50) {
					more = false;
				}
				for (IMessage message : pastMessages) {
					LocalDateTime messageTime = message.getTimestamp();
					// Check if earliest is after, aka earlier, but latest is before, aka still later.
					// If so, we've got a new latest allowed stamp.
					if (earliestAcceptableTimestamp == null || earliestMessage.getTimestamp().isAfter(messageTime)) {
						earliestMessage = message;
					}
					// Check if message was posted before our latest allowed timestamp.
					if (latestAllowed.isAfter(messageTime)) {
						channelHistory.add(message);
						continue;
					}
					// Message found that is current enough to be kept, no need to keep searching
					more = false;
				}
			}

			// If we're deleting our earliest message, we can't use it for lookups later.
			if (channelHistory.contains(earliestMessage)) {
				channelRetentionData.remove(channel.getID());
			} else {
				channelRetentionData.put(channel.getID(), earliestMessage);
			}
		}

		// Delete all the messages we've collected
		Iterator<IMessage> iterator = channelHistory.iterator();
		while (iterator.hasNext()) {
			IMessage message = iterator.next();
			iterator.remove();
			boolean undeleted = true;
			while (undeleted) {
				try {
					message.delete();
				} catch (MissingPermissionsException | DiscordException e) {
					e.printStackTrace();
				} catch (HTTP429Exception e) {
					e.printStackTrace();
					try {
						System.out.println("Rate limited, repeating request in a second.");
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e.printStackTrace();
						break;
					}
					continue;
				}
				break;
			}
		}
	}

	private Collection<IMessage> getPastMessages(Channel channel, @Nullable Integer limit, @Nullable String before, @Nullable String after) throws HTTP429Exception {
		StringBuilder request = new StringBuilder(DiscordEndpoints.CHANNELS).append(channel.getID()).append("/messages");
		if (limit == null && before == null && after == null) {
			if (channel.getMessages().size() >= 50) {
				// 50 is the default size returned. With no parameters, don't bother with a useless lookup.
				return channel.getMessages();
			} else {
				return getPastMessages(channel, request.toString());
			}
		}
		StringBuilder options = new StringBuilder("?");
		if (limit != null) {
			options.append("limit=").append(limit);
		}
		if (before != null) {
			if (options.length() > 1) {
				options.append('&');
			}
			options.append("before=").append(before);
		}
		if (after != null) {
			if (options.length() > 1) {
				options.append('&');
			}
			options.append("after=").append(after);
		}
		return getPastMessages(channel, request.append(options).toString());
	}

	private Collection<IMessage> getPastMessages(Channel channel, String request) throws HTTP429Exception {
		List<IMessage> messages = new ArrayList<>();
		String response;
		try {
			System.out.println("GET " + request);
			response = Requests.GET.makeRequest(request, new BasicNameValuePair("authorization", client.getToken()));
		} catch (DiscordException e) {
			e.printStackTrace();
			return messages;
		}

		if (response == null) {
			return messages;
		}

		MessageResponse[] msgs = new Gson().fromJson(response, MessageResponse[].class);

		for (MessageResponse message : msgs) {
			IMessage msg = DiscordUtils.getMessageFromJSON(client, channel, message);
			channel.addMessage(msg);
			messages.add(DiscordUtils.getMessageFromJSON(client, channel, message));
		}

		return messages;
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
				// Retrieve but do not yet remove the first element in the queue
				Triple<String, String, String> triple = queue.element();
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
				if (!client.getOurUser().getName().equals(triple.getMiddle())) {
					try {
						client.changeAccountInfo(Optional.of(triple.getMiddle()), login, password,
								Optional.empty());
					} catch (HTTP429Exception | DiscordException e) {
						// Trivial issue
					}
				}
				try {
					IMessage posted = group.sendMessage(triple.getRight());
					if (triple.getLeft().equals(channelMain) && !triple.getMiddle().equals(BOT_NAME)) {
						pastMainMessages.put(posted, triple.getMiddle());
					}
				} catch (MissingPermissionsException | DiscordException | HTTP429Exception e) {
					e.printStackTrace();
				}
				queue.remove();
			}
		}.runTaskTimerAsynchronously(getPlugin(), 20L, 20L);
	}

	public IDiscordClient getAPI() {
		return this.client;
	}

	public void logMessage(String message) {
		postMessage(BOT_NAME, message, channelLog);
	}

	public void postMessage(Message message, boolean global) {
		if (global) {
			postMessage((message.isThirdPerson() ? "* " : "") + message.getSenderName(),
					message.getDiscordMessage(), channelMain);
		}
		postMessage(BOT_NAME, message.getConsoleMessage(), channelLog);
	}

	public void postMessage(String name, String message, boolean global) {
		if (global) {
			postMessage(name, message, channelMain, channelLog);
		} else {
			postMessage(name, message, channelLog);
		}
	}

	public void postMessage(String name, String message, String... channels) {
		if (!isEnabled()) {
			return;
		}
		name = ChatColor.stripColor(name);
		// TODO allow formatting codes in any chat? Could support markdown rather than &codes.
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

	public void postReport(String message) {
		postMessage(Discord.BOT_NAME, message, channelReports);
	}

	public void setRetention(IGuild guild, IChannel channel, Long duration) {
		if (channelRetentionData.containsKey(channel.getID())) {
			channelRetentionData.remove(channel.getID());
		}
		discordData.set("retention." + guild.getID() + '.' + channel.getID(), duration);
	}

	public LoadingCache<Object, Object> getAuthCodes() {
		return authentications;
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
		String log = String.format("Command in #%s[%s] from %s[%s]: %s",
				channel.getName(), channel.getID(), user.getName(), user.getID(), command);
		logMessage(log);
		getLogger().info(log);
		String[] args = new String[split.length - 1];
		if (args.length > 0) {
			System.arraycopy(split, 1, args, 0, args.length);
		}
		commands.get(split[0]).execute(user, channel, args);
		return true;
	}

	protected void handleMinecraftCommandFor(DiscordPlayer player, String command, IChannel channel) {
		if (player.hasPendingCommand()) {
			postMessage(Discord.BOT_NAME, "You already have a pending command. Please be patient.", channel.getID());
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
					postMessage(Discord.BOT_NAME, "Command " + command + " from " + player.getName() + " timed out.", channel.getID());
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
				postMessage(Discord.BOT_NAME, message, channel.getID());
			}
		}.runTaskAsynchronously(getPlugin());
	}

	protected void handleChatToMinecraft(IMessage message, Player player) {
		String content = message.getContent();
		if (!player.hasPermission("sblock.discord.filterexempt")) {
			int newline = content.indexOf('\n');
			boolean delete = false;
			if (newline > 0) {
				postMessage(Discord.BOT_NAME, "Newlines are not allowed in messages to Minecraft, <@"
						+ message.getAuthor().getID() + ">", message.getChannel().getID());
				delete = true;
			} else if (content.length() > 255) {
				postMessage(Discord.BOT_NAME, "Messages from Discord may not be over 255 characters, <@"
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

	@Override
	public String getName() {
		return "Discord";
	}

}
