package com.easterlyn.discord;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.message.Message;
import com.easterlyn.discord.abstraction.DiscordCommand;
import com.easterlyn.discord.abstraction.DiscordModule;
import com.easterlyn.discord.queue.CallPriority;
import com.easterlyn.discord.queue.CallType;
import com.easterlyn.discord.queue.DiscordCallable;
import com.easterlyn.discord.queue.DiscordQueue;
import com.easterlyn.module.Module;
import com.easterlyn.utilities.player.PermissiblePlayer;
import com.easterlyn.utilities.player.PlayerUtils;
import com.easterlyn.utilities.concurrent.ConcurrentConfiguration;
import com.easterlyn.utilities.tuple.Pair;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Module for managing messaging to and from Discord.
 *
 * @author Jikoo
 */
public class Discord extends Module {

	private final String chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private final Pattern toEscape = Pattern.compile("([_~*])");
	private final Map<Class<? extends DiscordModule>, DiscordModule> modules;
	private final Map<String, DiscordCommand> commands;
	private final LoadingCache<Object, Object> authentications;
	private final ConcurrentConfiguration discordData;
	private final StringBuffer logBuffer;

	private Language lang;
	private String  token;
	private IDiscordClient client;
	private BukkitTask heartbeatTask;
	private DiscordQueue drainQueueThread;
	private boolean ready = false;
	private long lastLog = System.currentTimeMillis();

	private boolean lock = false;

	public Discord(Easterlyn plugin) {
		super(plugin);

		modules = new HashMap<>();
		commands = new HashMap<>();

		authentications = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build(
				new CacheLoader<Object, Object>() {
					@Override
					public Object load(Object key) throws Exception {
						if (!(key instanceof UUID)) {
							throw new IllegalArgumentException("Key must be a UUID");
						}
						String code = generateUniqueCode();
						authentications.put(code, key);
						return code;
					}
				});

		File file = new File(plugin.getDataFolder(), "DiscordData.yml");
		if (file.exists()) {
			discordData = ConcurrentConfiguration.load(file);
		} else {
			discordData = new ConcurrentConfiguration();
		}

		logBuffer = new StringBuffer(1500);
	}

	@SuppressWarnings("rawtypes") // I don't like doing this, but type erasure forces my hand.
	@Override
	protected void onEnable() {
		this.lang = getPlugin().getModule(Language.class);
		token = getConfig().getString("discord.token");

		if (token == null) {
			getLogger().severe("Unable to connect to Discord, no connection details provided!");
			this.disable();
			return;
		}

		try {
			this.client = new ClientBuilder().withToken(token).setMaxMessageCacheCount(-1).build();
		} catch (DiscordException e) {
			e.printStackTrace();
			this.disable();
			return;
		}

		Reflections reflections = new Reflections("com.easterlyn.discord.listeners");
		Set<Class<? extends IListener>> listenerClasses = reflections.getSubTypesOf(IListener.class);
		EventDispatcher dispatcher = this.client.getDispatcher();
		for (Class<? extends IListener> clazz : listenerClasses) {
			if (Modifier.isAbstract(clazz.getModifiers())) {
				continue;
			}
			if (Easterlyn.areDependenciesMissing(clazz)) {
				getLogger().warning(clazz.getSimpleName() + " is missing dependencies, skipping.");
				continue;
			}
			try {
				Constructor<? extends IListener> constructor = clazz.getConstructor(getClass());
				IListener listener = constructor.newInstance(this);
				dispatcher.registerListener(listener);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		new Thread(() -> {
			try {
				client.login();
			} catch (DiscordException e) {
				client = null;
			} catch (RateLimitException e) {
				try {
					Thread.sleep(e.getRetryDelay() + 1000L);
					client.login();
				} catch (InterruptedException | DiscordException | RateLimitException e1) {
					// Retry failed, ignore
				}
			}
		}, "Easterlyn-DiscordLogin").start();

		// Only load modules and whatnot once, no matter how many times we re-enable
		if (lock) {
			return;
		}

		lock = true;

		reflections = new Reflections("com.easterlyn.discord.modules");
		Set<Class<? extends DiscordModule>> moduleClazzes = reflections.getSubTypesOf(DiscordModule.class);
		for (Class<? extends DiscordModule> clazz : moduleClazzes) {
			if (Modifier.isAbstract(clazz.getModifiers())) {
				continue;
			}
			if (Easterlyn.areDependenciesMissing(clazz)) {
				getLogger().warning(clazz.getSimpleName() + " is missing dependencies, skipping.");
				continue;
			}
			try {
				Constructor<? extends DiscordModule> constructor = clazz.getConstructor(getClass());
				DiscordModule module = constructor.newInstance(this);
				modules.put(clazz, module);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		reflections = new Reflections("com.easterlyn.discord.commands");
		Set<Class<? extends DiscordCommand>> commandClazzes = reflections.getSubTypesOf(DiscordCommand.class);
		for (Class<? extends DiscordCommand> clazz : commandClazzes) {
			if (Modifier.isAbstract(clazz.getModifiers())) {
				continue;
			}
			if (Easterlyn.areDependenciesMissing(clazz)) {
				getLogger().warning(clazz.getSimpleName() + " is missing dependencies, skipping.");
				continue;
			}
			try {
				Constructor<? extends DiscordCommand> constructor = clazz.getConstructor(getClass());
				DiscordCommand command = constructor.newInstance(this);
				commands.put('/' + command.getName(), command);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDisable() {
		this.ready = false;
		try {
			discordData.save(new File(getPlugin().getDataFolder(), "DiscordData.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (client != null) {
			/*
			 * Discord4J calls Thread#interrupt when finishing DiscordClientImpl#logout. Spawning
			 * new threads when shutting down is a bad idea, but we can't do much else. In case
			 * we're not stopping and are instead reloading the Discord module, we need to keep a
			 * reference to the current client to be able to log it out.
			 */
			final IDiscordClient oldClient = client;
			new Thread(() -> {
				try {
					oldClient.logout();
				} catch (DiscordException e) {
					e.printStackTrace();
				}
			}, "Easterlyn-DiscordLogout").start();
		}
	}

	@Override
	public YamlConfiguration loadConfig() {
		super.loadConfig();

		token = getConfig().getString("discord.token");

		return getConfig();
	}

	private String generateUniqueCode() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
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

	public void setReady(boolean ready) {
		if (ready) {
			for (DiscordModule module : modules.values()) {
				module.doSetup();
			}
			startQueueDrain();
			startHeartbeat();
		}
		this.ready = ready;
	}

	public boolean isReady() {
		return this.ready;
	}

	public Configuration getDatastore() {
		return discordData;
	}

	private void startHeartbeat() {
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
				// In case the queue has encountered an error, attempt to restart
				startQueueDrain();

				for (DiscordModule module : modules.values()) {
					try {
						module.doHeartbeat();
					} catch (Exception e) {
						// Catch any exception so everything else doesn't break
						e.printStackTrace();
					}
				}
				if (logBuffer.length() > 0) {
					long now = System.currentTimeMillis();
					if (now > lastLog + 30000 || logBuffer.length() > 1500) {
						for (long channelID : getLogChannelIDs()) {
							IChannel channel = getClient().getChannelByID(channelID);
							if (channel != null) {
								addMessageToQueue(channel, null, logBuffer.toString());
							}
						}
						logBuffer.delete(0, logBuffer.length());
						lastLog = now;
					}
				}
			}
		}.runTaskTimerAsynchronously(getPlugin(), 100L, 6000L); // 5 minutes between checks
	}

	private void startQueueDrain() {
		if (!this.isEnabled()) {
			return;
		}
		if (drainQueueThread == null || !drainQueueThread.isAlive()) {
			drainQueueThread = new DiscordQueue(this, 50, "Easterlyn-DiscordQueue");
			drainQueueThread.start();
		}
	}

	public void queue(DiscordCallable call) {
		if (!this.isEnabled()) {
			return;
		}

		startQueueDrain();
		drainQueueThread.queue(call.getChainStart());
	}

	public void queueMessageDeletion(CallPriority priority, IMessage... messages) {
		if (!this.isEnabled()) {
			return;
		}

		if (messages.length == 0) {
			return;
		}

		startQueueDrain();

		if (messages.length == 1) {
			queueSingleDelete(priority, messages[0]);
			return;
		}

		queueMessageDeletion(priority, Arrays.asList(messages));
	}

	public void queueMessageDeletion(CallPriority priority, Collection<IMessage> messages) {
		if (!this.isEnabled()) {
			return;
		}

		// Bulk delete requires messages to be within the last 14 days. To be safe, we pad our check.
		Instant bulkDeleteableBefore = Instant.now().plus(13, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS);
		// Collect messages by channel to ensure bulk delete will work.
		Map<Pair<IChannel, Boolean>, List<IMessage>> messagesByChannel = messages.stream().distinct()
				.collect(Collectors.groupingBy(
						message -> new Pair<>(message.getChannel(),
								message.getTimestamp().isBefore(bulkDeleteableBefore))));

		for (Map.Entry<Pair<IChannel, Boolean>, List<IMessage>> entry : messagesByChannel.entrySet()) {

			if (entry.getValue().size() == 1) {
				// Single message, single delete.
				queueSingleDelete(priority, entry.getValue().get(0));
				continue;
			}

			if (!entry.getKey().getRight()) {
				// Messages older than 14 days, single delete all
				for (IMessage message : entry.getValue()) {
					queueSingleDelete(priority, message);
				}
				continue;
			}

			while (entry.getValue().size() > 0) {
				List<IMessage> subList = entry.getValue().subList(0, Math.min(100, entry.getValue().size()));
				List<IMessage> messageList = new ArrayList<>();
				messageList.addAll(subList);
				subList.clear();
				this.queue(new DiscordCallable(entry.getKey().getLeft().getGuild().getLongID(), CallType.BULK_DELETE) {
					@Override
					public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
						entry.getKey().getLeft().bulkDelete(messageList);
					}
				}.withRetries(1));
			}
		}
	}

	private void queueSingleDelete(CallPriority priority, IMessage message) {
		if (!this.isEnabled()) {
			return;
		}

		startQueueDrain();
		drainQueueThread.queue(new DiscordCallable(message.getGuild().getLongID(), CallType.MESSAGE_DELETE) {
			@Override
			public void call() throws MissingPermissionsException, RateLimitException, DiscordException {
				message.delete();
			}
		}.withRetries(1));
	}

	public IDiscordClient getClient() {
		return this.client;
	}

	public long getGeneralChannelID(IGuild guild) {
		return this.getChannelID(guild, "general");
	}

	public Collection<Long> getGeneralChannelIDs() {
		return this.getChannelIDs("general");
	}

	public long getMainChannelID(IGuild guild) {
		return this.getChannelID(guild, "main");
	}

	public Collection<Long> getMainChannelIDs() {
		return this.getChannelIDs("main");
	}

	public long getLogChannelID(IGuild guild) {
		return this.getChannelID(guild, "log");
	}

	public Collection<Long> getLogChannelIDs() {
		return this.getChannelIDs("log");
	}

	public long getReportChannelID(IGuild guild) {
		return this.getChannelID(guild, "report");
	}

	public Collection<Long> getReportChannelIDs() {
		return this.getChannelIDs("report");
	}

	private long getChannelID(IGuild guild, String type) {
		return this.getConfig().getLong("guilds." + guild.getLongID() + ".channels." + type);
	}

	private Collection<Long> getChannelIDs(String type) {
		List<Long> list = new ArrayList<>();
		if (!this.isEnabled()) {
			return list;
		}
		for (String guildIDString : this.getConfig().getConfigurationSection("guilds").getKeys(false)) {

			// Parse guild ID
			long guildID;
			try {
				guildID = Long.parseLong(guildIDString);
			} catch (NumberFormatException e) {
				continue;
			}

			IGuild guild = this.getClient().getGuildByID(guildID);
			// Ensure valid guild
			if (guild == null) {
				continue;
			}

			long channelID = this.getChannelID(guild, type);
			// Ensure channel set
			if (channelID == 0) {
				continue;
			}

			list.add(channelID);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public <T extends DiscordModule> T getModule(Class<T> clazz) throws IllegalArgumentException {
		Preconditions.checkArgument(DiscordModule.class.isAssignableFrom(clazz), clazz.getName() + " is not a DiscordModule.");
		Preconditions.checkArgument(modules.containsKey(clazz), "Module not enabled!");
		Object object = modules.get(clazz);
		Preconditions.checkArgument(clazz.isAssignableFrom(object.getClass()));
		return (T) object;
	}

	public void log(String message) {
		postMessage(null, message, getLogChannelIDs());
	}

	public void postMessage(Message message, boolean global) {
		if (global) {
			postMessage((message.isThirdPerson() ? "* " : "") + message.getSenderName(),
					message.getDiscordMessage(), getMainChannelIDs());
		}
		postMessage(null, message.getConsoleMessage(), getLogChannelIDs());
	}

	public void postMessage(String name, String message, boolean global) {
		if (global) {
			Collection<Long> channels = getMainChannelIDs();
			channels.addAll(getLogChannelIDs());
			postMessage(name, message, channels);
		} else {
			postMessage(name, message, getLogChannelIDs());
		}
	}

	public void postMessage(String name, String message, Collection<Long> channelIDs) {
		if (channelIDs.isEmpty()) {
			return;
		}
		this.postMessage(name, message, channelIDs.toArray(new Long[channelIDs.size()]));
	}

	public void postMessage(String name, String message, Long... channelIDs) {
		if (!isEnabled() || channelIDs.length == 0) {
			return;
		}

		if (name != null) {
			name = ChatColor.stripColor(name);
		}
		// TODO allow formatting codes in any chat? Could support markdown rather than &codes.
		message = ChatColor.stripColor(message);
		if (message.trim().isEmpty()) {
			return;
		}
		// Discord is case-sensitive. This prevents an @everyone alert without altering content.
		message = message.replace("@everyone", "@Everyone").replace("@here", "@Here");
		for (int index = 0, nextIndex = logBuffer.length() > 0 ? 1999 - logBuffer.length() : 2000;
				index < message.length(); index = nextIndex, nextIndex += 2000) {
			// TODO: split at logical areas - spaces, dashes, etc. rather than a hard limit
			if (nextIndex > message.length()) {
				nextIndex = message.length();
			}
			String nextMessage = message.substring(index, nextIndex);
			for (long channelID : channelIDs) {
				IChannel channel = this.getClient().getChannelByID(channelID);
				if (channel == null) {
					IUser user = client.getUserByID(channelID);
					if (user == null) {
						return;
					}
					try {
						channel = client.getOrCreatePMChannel(user);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
					return;
				}
				if (!channel.isPrivate() && channelID == this.getLogChannelID(channel.getGuild())) {
					long now = System.currentTimeMillis();
					if (logBuffer.length() != 0) {
						logBuffer.append('\n');
					}
					logBuffer.append(nextMessage);
					if (now > lastLog + 30000 || logBuffer.length() > 1500) {
						addMessageToQueue(channel, null, logBuffer.toString());
						logBuffer.delete(0, logBuffer.length());
						lastLog = now;
					}
					continue;
				}
				addMessageToQueue(channel, name, nextMessage);
			}
		}
	}

	@Nullable
	private DiscordCallable addMessageToQueue(final IChannel channel, final String name, final String message) {
		if (drainQueueThread != null) {
			// Ensure bot has finished logging in.
			return drainQueueThread.queueMessage(channel, name, message);
		}
		return null;
	}

	public void postReport(String message) {
		postMessage(null, message, getReportChannelIDs());
	}

	/**
	 * Update a Discord user's status. Handles new users (alerts them they must link, then kicks after a day if they have not)
	 *
	 * @param user the IUser
	 */
	public void updateUser(IUser user) {
		UUID uuid = this.getUUIDOf(user);

		if (uuid == null) {
			this.updateUnlinkedUser(user);
			return;
		}

		this.updateLinkedUser(user, uuid);
	}

	private void updateUnlinkedUser(IUser user) {
		if (this.isLinked(user) || this.getClient().getOurUser().equals(user) || user.isBot()) {
			return;
		}

		long now = System.currentTimeMillis();

		this.getClient().getGuilds().forEach(guild -> {
			if (!guild.getUsers().contains(user)) {
				return;
			}

			// Check if a user has roles - if they have roles, they're recognized.
			// N.B.: Discord4J explicitly declares users to have the @everyone role
			for (IRole role : guild.getRolesForUser(user)) {
				if (!role.equals(guild.getEveryoneRole())) {
					return;
				}
			}

			String path = "unlinked." + guild.getLongID() + '.'  + user.getLongID();

			if (!discordData.isSet(path)) {
				// 1 day grace period
				discordData.set(path, now + 86400000);
				this.postMessage(null,
						lang.getValue("discord.link.mandate").replace("{USER}", user.mention()),
						this.getGeneralChannelID(guild));
				return;
			}

			long kickTime = discordData.getLong(path);

			if (kickTime <= now) {
				discordData.set(path, null);
				queue(new DiscordCallable(user.getOrCreatePMChannel().getLongID(), CallType.MESSAGE_SEND) {
					@Override
					public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
						user.getOrCreatePMChannel().sendMessage(lang.getValue("discord.link.graceless"));
					}
				}.withChainedCall(new DiscordCallable(guild.getLongID(), CallType.GUILD_USER_KICK) {
					@Override
					public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
						guild.kickUser(user);
					}
				}));
			}
		});
	}

	/**
	 * Update a linked Minecraft player's Discord account. Includes nicknaming and automatic role
	 * management.
	 *
	 * @param user the user, or null if it is to be searched for
	 * @param uuid the UUID of the player
	 */
	public void updateLinkedUser(@Nullable IUser user, UUID uuid) {

		if (user == null) {
			// Given player object, find user ID from UUID
			String uuidString = uuid.toString();
			for (String path : discordData.getConfigurationSection("users").getKeys(false)) {
				try {
					if (uuidString.equals(discordData.getString("users." + path))) {
						user = getClient().getUserByID(Long.parseLong(path));
						break;
					}
				} catch (NumberFormatException e) {}
			}
		}

		if (user == null) {
			return;
		}

		// Yes, yes, this is final now, shut up compiler.
		final IUser iuser = user;

		// Load a permissible player
		Player loadedPlayer = PlayerUtils.getPlayer(getPlugin(), uuid);
		if (loadedPlayer == null) {
			return;
		}
		if (!loadedPlayer.isOnline() && !(loadedPlayer instanceof PermissiblePlayer)) {
			loadedPlayer = new PermissiblePlayer(loadedPlayer);
		}

		// Finalize for callable
		final Player player = loadedPlayer;

		for (IGuild guild : this.getClient().getGuilds()) {
			if (guild.getUserByID(user.getLongID()) == null) {
				continue;
			}

			ConfigurationSection guildRoles = this.getConfig().getConfigurationSection("guilds." + guild.getLongID() + ".roles");
			if (guildRoles == null) {
				continue;
			}

			final List<IRole> roles = new ArrayList<>();
			for (String roleName : guildRoles.getKeys(false)) {
				if (!player.hasPermission("easterlyn.group." + roleName)) {
					continue;
				}

				long roleID = guildRoles.getLong(roleName);
				IRole role = guild.getRoleByID(roleID);
				if (role == null) {
					continue;
				}
				roles.add(role);
			}

			List<IRole> currentRoles = iuser.getRolesForGuild(guild);
			if (!currentRoles.containsAll(roles)) {
				for (IRole role : currentRoles) {
					if (!roles.contains(role)) {
						roles.add(role);
					}
				}

				IRole[] roleArray = roles.toArray(new IRole[roles.size()]);
				this.queue(new DiscordCallable(guild.getLongID(), CallType.GUILD_USER_ROLE) {
					@Override
					public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
						guild.editUserRoles(iuser, roleArray);
					}
				});
			}

			if (!this.getConfig().getBoolean("linkName")) {
				continue;
			}

			if (player.getName() != null) {
				String name = iuser.getNicknameForGuild(guild);
				if (!player.getName().equals(name)) {
					this.queue(new DiscordCallable(guild.getLongID(), CallType.GUILD_USER_NICKNAME) {
						@Override
						public void call() throws DiscordException, RateLimitException, MissingPermissionsException {
							guild.setUserNickname(iuser, player.getName());
						}
					});
				}
			}
		}
	}

	public LoadingCache<Object, Object> getAuthCodes() {
		return authentications;
	}

	public boolean isLinked(IUser user) {
		return discordData.isString("users." + user.getLongID());
	}

	public UUID getUUIDOf(IUser user) {
		String uuidString = discordData.getString("users." + user.getLongID());
		if (uuidString == null) {
			return null;
		}
		return UUID.fromString(uuidString);
	}

	public void addLink(UUID uuid, IUser user) {
		discordData.set("users." + user.getLongID(), uuid.toString());

		updateLinkedUser(user, uuid);
	}

	@Nullable
	public DiscordPlayer getDiscordPlayerFor(IUser user) {
		UUID uuid = getUUIDOf(user);
		if (uuid == null) {
			return null;
		}
		Player player = PlayerUtils.getPlayer(this.getPlugin(), uuid);
		if (player == null) {
			return  null;
		}
		if (player instanceof DiscordPlayer) {
			return (DiscordPlayer) player;
		}
		// PlayerUtils loads a PermissiblePlayer, wrapping a wrapper would be silly.
		DiscordPlayer dplayer = new DiscordPlayer(this, user, player.getPlayer());
		PlayerUtils.modifyCachedPlayer(dplayer);
		return dplayer;
	}

	public boolean handleDiscordCommand(String command, IUser user, IChannel channel) {
		String[] split = command.split("\\s");
		if (!commands.containsKey(split[0])) {
			return false;
		}
		String[] args = new String[split.length - 1];
		if (args.length > 0) {
			System.arraycopy(split, 1, args, 0, args.length);
		}
		commands.get(split[0]).execute(user, channel, command, args);
		return true;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "Discord";
	}

}
