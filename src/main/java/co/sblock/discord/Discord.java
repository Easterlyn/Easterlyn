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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import sx.blah.discord.handle.obj.IUser;
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
	private final ConcurrentLinkedQueue<DiscordCallable> queue;
	private final Map<String, DiscordCommand> commands;
	private final LoadingCache<Object, Object> authentications;
	private final YamlConfiguration discordData;
	private final Cache<IMessage, String> pastMainMessages;
	private final Map<String, Pair<IMessage, Boolean>> channelRetentionData;

	private String channelMain, channelLog, channelReports;
	private Optional<String> login, password;
	private IDiscordClient client;
	private Users users;
	private ChannelManager manager;
	private MessageBuilder builder;
	private BukkitTask heartbeatTask;
	private Thread queueDrainThread;

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

		pastMainMessages = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(10)
				.removalListener(new RemovalListener<IMessage, String>() {
					@Override
					public void onRemoval(RemovalNotification<IMessage, String> notification) {
						queue.add(new DiscordCallable() {
							@Override
							public void call() throws MissingPermissionsException, HTTP429Exception, DiscordException {
								// Editing messages causes them to use the current name.
								resetBotName();
								notification.getKey().edit(notification.getValue());
							}

							@Override
							public boolean retryOnException() {
								return true;
							}
						});
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
			resetBotName();
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

	private void resetBotName() {
		if (!client.getOurUser().getName().equals(BOT_NAME)) {
			try {
				client.changeAccountInfo(Optional.of(BOT_NAME), login, password, Optional.empty());
			} catch (HTTP429Exception | DiscordException e) {
				// Nothing we can do about this, really
			}
		}
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
				resetBotName();
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
		}.runTaskTimerAsynchronously(getPlugin(), 20L, 12000L); // 10 minutes between checks
	}

	/**
	 * This method is blocking and will run until either the bot is rate limited or messages old
	 * enough have been deleted.
	 * 
	 * @param channel the IChannel to do retention for
	 * @param duration the duration in seconds
	 */
	private void doRetention(IChannel channel, long duration) {
		// This method requires touching a lot of Discord4J internals.
		if (duration == -1 || !(channel instanceof Channel)) {
			return;
		}

		// Ensure we can read history and delete messages
		try {
			DiscordUtils.checkPermissions(client, channel, EnumSet.of(Permissions.READ_MESSAGE_HISTORY, Permissions.MANAGE_MESSAGES));
		} catch (MissingPermissionsException e) {
			getLogger().warning("Unable to do retention for channel " + channel.mention() + " - cannot read history and delete messages!");
			return;
		}

		/*
		 * Channel history must be populated here. To reduce our usage of Discord's API, we have 2
		 * search modes. When searching backwards, we store the last found valid message and proceed
		 * farther back until we run out of messages. When searching forwards, we use our stored
		 * last valid message to search (if needed) until a timestamp within the retention period is
		 * encountered.
		 */

		List<IMessage> channelHistory = new ArrayList<>();
		LocalDateTime latestAllowedTime = LocalDateTime.now().minusSeconds(duration);
		IMessage earliestRemaining = null, currentTarget = null;
		boolean searchBack = true;
		if (channelRetentionData.containsKey(channel.getID())) {
			Pair<IMessage, Boolean> triple = channelRetentionData.get(channel.getID());
			currentTarget = triple.getLeft();
			searchBack = triple.getRight();
			if (!searchBack && currentTarget.getTimestamp().isAfter(latestAllowedTime)) {
				return;
			}
		}
		boolean more = true;
		while (more && channelHistory.size() < 2000) {
			// Pause for a second to prevent rate limiting
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			Collection<IMessage> pastMessages;
			try {
				pastMessages = getPastMessages((Channel) channel, currentTarget, searchBack);
			} catch (HTTP429Exception e) {
				try {
					Thread.sleep(e.getRetryDelay());
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				continue;
			} catch (DiscordException e) {
				// An error occurred fetching past messages.
				// This usually means the message ID being used to search is invalid.
				channelRetentionData.remove(channel.getID());
				return;
			}

			if (pastMessages.size() == 0) {
				/*
				 * Channel has no messages.
				 * 
				 * The reason we do not store this data and not re-query is that the vast majority
				 * of channels with retention will not have short enough retention periods to ever
				 * fully drain. The only channel that regularly will be able to is #main at night.
				 * 
				 * Basically, it's a very minor evil that costs me more labor to account for than
				 * it's worth. We make hundreds of Discord queries, one more every ten minutes
				 * won't make or break this.
				 */
				return;
			}

			// Channel has under 50 messages or search back is complete
			if (pastMessages.size() < 50) {
				more = false;
				searchBack = false;
			}

			for (IMessage message : pastMessages) {
				LocalDateTime messageTime = message.getTimestamp();
				if (currentTarget == null) {
					currentTarget = message;
				}
				if (searchBack) {
					// Earlier timestamp found, farther back we go.
					if (currentTarget.getTimestamp().isAfter(messageTime)) {
						currentTarget = message;
					}
				}
				// Too old, delete
				if (latestAllowedTime.isAfter(messageTime)) {
					channelHistory.add(message);
					continue;
				}
				if (!searchBack) {
					// Message found that is current enough to be kept, no need to keep searching
					more = false;
				}
				// Not too old, set to current if none specified or if older than current
				if (earliestRemaining == null || earliestRemaining.getTimestamp().isBefore(messageTime)) {
					earliestRemaining = message;
					if (!searchBack) {
						currentTarget = message;
					}
					continue;
				}
			}
		}

		if (currentTarget != null) {
			if (channelHistory.contains(currentTarget)) {
				// Current target is being deleted, use earliest remaining instead to avoid re-determining
				currentTarget = earliestRemaining;
			}
		}

		channelRetentionData.put(channel.getID(), new ImmutablePair<>(currentTarget, searchBack));

		// Delete all the messages we've collected
		Iterator<IMessage> iterator = channelHistory.iterator();
		while (iterator.hasNext()) {
			IMessage message = iterator.next();
			iterator.remove();
			queue.add(new DiscordCallable() {
				@Override
				public void call() throws MissingPermissionsException, HTTP429Exception, DiscordException {
					message.delete();
				}

				@Override
				public boolean retryOnException() {
					return false;
				}
			});
		}
	}

	private Collection<IMessage> getPastMessages(Channel channel, IMessage message, boolean back)
			throws HTTP429Exception, DiscordException {
		return getPastMessages(channel, 50, back && message != null ? message.getID() : null, back
				&& message != null ? message.getID() : null);
	}

	private Collection<IMessage> getPastMessages(Channel channel, @Nullable Integer limit,
			@Nullable String before, @Nullable String after)
					throws HTTP429Exception, DiscordException {
		StringBuilder request = new StringBuilder(DiscordEndpoints.CHANNELS)
				.append(channel.getID()).append("/messages");
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

	private Collection<IMessage> getPastMessages(Channel channel, String request) throws HTTP429Exception, DiscordException {
		List<IMessage> messages = new ArrayList<>();
		String response;
		System.out.println("GET " + request);
		response = Requests.GET.makeRequest(request, new BasicNameValuePair("authorization", client.getToken()));

		if (response == null) {
			return messages;
		}

		MessageResponse[] msgs = new Gson().fromJson(response, MessageResponse[].class);

		for (MessageResponse message : msgs) {
			IMessage msg = DiscordUtils.getMessageFromJSON(client, channel, message);
			//channel.addMessage(msg);
			messages.add(msg);
		}

		return messages;
	}

	protected void startQueueDrain() {
		if (queueDrainThread != null && queueDrainThread.isAlive()) {
			return;
		}
		queueDrainThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (isEnabled()) {
					// Sleep for 1/4 of a second
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (queue.isEmpty()) {
						continue;
					}

					DiscordCallable callable = queue.element();
					try {
						callable.call();
					} catch (DiscordException e) {
						if (callable.retryOnException()) {
							// Don't log when retrying, we only retry because of a Discord4J fault generally.
							continue;
						}
						e.printStackTrace();
					} catch (HTTP429Exception e) {
						try {
							// Pause, we're rate limited.
							Thread.sleep(e.getRetryDelay());
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					} catch (Exception e) {
						// Likely permissions, but can be malformed JSON when odd responses are received
						e.printStackTrace();
					}
					queue.remove();
				}
			}
		}, "Sblock-DiscordQueue");

		queueDrainThread.start();
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
			addMessageToQueue(channel, name, builder.toString());
		}
	}

	private void addMessageToQueue(final String channel, final String name, final String message) {
		queue.add(new DiscordCallable() {
			@Override
			public void call() throws MissingPermissionsException, HTTP429Exception, DiscordException {
				IChannel group = client.getChannelByID(channel);
				if (group == null) {
					IUser user = client.getUserByID(channel);
					if (user == null) {
						return;
					}
					try {
						group = client.getOrCreatePMChannel(user);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
				if (!client.getOurUser().getName().equals(name)) {
					try {
						client.changeAccountInfo(Optional.of(name), login, password,
								Optional.empty());
					} catch (HTTP429Exception | DiscordException e) {
						// Trivial issue
					}
				}
				IMessage posted = group.sendMessage(message);
				if (channel.equals(channelMain) && !name.equals(BOT_NAME)) {
					StringBuilder builder = new StringBuilder().append("**")
							.append(toEscape.matcher(name).replaceAll("\\\\$1"));
					if (!name.startsWith("* ")) {
						builder.append(':');
					}
					builder.append("** ").append(message);
					pastMainMessages.put(posted, builder.toString());
				}
				try {
					/*
					 * Sleep .75 seconds after posting a message to avoid being rate limited. This
					 * will still get us hit when continuously posting, but it allows for much more
					 * responsive chat. As we're a small server and chat is often calm, I'm not
					 * going to worry about it.
					 */
					Thread.sleep(750);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			@Override
			public boolean retryOnException() {
				return false;
			}
		});
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
