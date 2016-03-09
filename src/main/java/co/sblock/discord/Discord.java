package co.sblock.discord;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import org.reflections.Reflections;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import co.sblock.Sblock;
import co.sblock.chat.message.Message;
import co.sblock.discord.abstraction.CallPriority;
import co.sblock.discord.abstraction.DiscordCallable;
import co.sblock.discord.abstraction.DiscordCommand;
import co.sblock.discord.abstraction.DiscordModule;
import co.sblock.discord.listeners.DiscordMessageAcknowledgedListener;
import co.sblock.discord.listeners.DiscordMessageDeleteListener;
import co.sblock.discord.listeners.DiscordMessageReceivedListener;
import co.sblock.discord.listeners.DiscordReadyListener;
import co.sblock.module.Module;
import co.sblock.utilities.PlayerLoader;
import co.sblock.utilities.TextUtils;

import net.md_5.bungee.api.ChatColor;

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
			spaceword = Pattern.compile("(\\s*)(\\S*)");
	private final Map<Class<? extends DiscordModule>, DiscordModule> modules;
	private final Map<String, DiscordCommand> commands;
	private final LoadingCache<Object, Object> authentications;
	private final YamlConfiguration discordData;

	private String botName, channelMain, channelLog, channelReports;
	private String login, password;
	private IDiscordClient client;
	private BukkitTask heartbeatTask;
	private QueueDrainThread drainQueueThread;

	private boolean lock = false;

	public Discord(Sblock plugin) {
		super(plugin);

		modules = new HashMap<>();
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
	}

	@Override
	protected void onEnable() {
		botName = getConfig().getString("discord.username", "Sbot");
		login = getConfig().getString("discord.login");
		password = getConfig().getString("discord.password");
		channelMain = getConfig().getString("discord.chat.main");
		channelLog = getConfig().getString("discord.chat.log");
		channelReports = getConfig().getString("discord.chat.reports");

		if (login == null || password == null) {
			getLogger().severe("Unable to connect to Discord, no username or password!");
			this.disable();
			return;
		}

		try {
			this.client = new ClientBuilder().withLogin(this.login, this.password).build();
		} catch (DiscordException e) {
			e.printStackTrace();
			this.disable();
			return;
		}

		EventDispatcher dispatcher = this.client.getDispatcher();
		dispatcher.registerListener(new DiscordReadyListener(this));
		dispatcher.registerListener(new DiscordMessageReceivedListener(this));
		dispatcher.registerListener(new DiscordMessageDeleteListener(this));
		dispatcher.registerListener(new DiscordMessageAcknowledgedListener(this));

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					client.login();
				} catch (DiscordException e) {
					client = null;
				}
			}
		}, "Sblock-DiscordLogin").start();

		// Only load modules and whatnot once, no matter how many times we re-enable
		if (lock) {
			return;
		}

		lock = true;

		Reflections reflections = new Reflections("co.sblock.discord.modules");
		Set<Class<? extends DiscordModule>> moduleClazzes = reflections.getSubTypesOf(DiscordModule.class);
		for (Class<? extends DiscordModule> clazz : moduleClazzes) {
			if (Modifier.isAbstract(clazz.getModifiers())) {
				continue;
			}
			if (!Sblock.areDependenciesPresent(clazz)) {
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

		reflections = new Reflections("co.sblock.discord.commands");
		Set<Class<? extends DiscordCommand>> commandClazzes = reflections.getSubTypesOf(DiscordCommand.class);
		for (Class<? extends DiscordCommand> clazz : commandClazzes) {
			if (Modifier.isAbstract(clazz.getModifiers())) {
				continue;
			}
			if (!Sblock.areDependenciesPresent(clazz)) {
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
		if (client != null) {
			resetBotName();
			/*
			 * Discord4J calls Thread#interrupt when finishing DiscordClientImpl#logout. Spawning
			 * new threads when shutting down is a bad idea, but we can't do much else. In case
			 * we're not stopping and are instead reloading the Discord module, we need to keep a
			 * reference to the current client to be able to log it out.
			 */
			final IDiscordClient oldClient = client;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						oldClient.logout();
					} catch (HTTP429Exception | DiscordException e) {
						e.printStackTrace();
					}
				}
			}, "Sblock-DiscordLogout").start();
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

		botName = getConfig().getString("discord.username", "Sbot");
		login = getConfig().getString("discord.login");
		password = getConfig().getString("discord.password");
		channelMain = getConfig().getString("discord.chat.main");
		channelLog = getConfig().getString("discord.chat.log");
		channelReports = getConfig().getString("discord.chat.reports");

		return getConfig();
	}

	public String getBotName() {
		return botName;
	}

	private void resetBotName() {
		if (!client.getOurUser().getName().equals(this.getBotName())) {
			try {
				client.changeUsername(this.getBotName());
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

	public YamlConfiguration getDatastore() {
		return discordData;
	}

	public void startHeartbeat() {
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
				// In case either queue has encountered an error, attempt to restart them
				startQueueDrain();

				resetBotName();

				for (DiscordModule module : modules.values()) {
					module.doHeartbeat();
				}
			}
		}.runTaskTimerAsynchronously(getPlugin(), 20L, 6000L); // 5 minutes between checks
	}

	private void startQueueDrain() {
		if (drainQueueThread == null || !drainQueueThread.isAlive()) {
			drainQueueThread = new QueueDrainThread(this, 150, "Sblock-DiscordQueue");
			drainQueueThread.start();
		}
	}

	public void queue(DiscordCallable call) {
		drainQueueThread.queue(call);
	}

	public void queueMessageDeletion(IMessage message, CallPriority priority) {
		drainQueueThread.queue(new DiscordCallable(priority, false) {
			@Override
			public void call() throws MissingPermissionsException, HTTP429Exception, DiscordException {
				message.delete();
			}
		});
	}

	public IDiscordClient getClient() {
		return this.client;
	}

	public String getMainChannel() {
		return this.channelMain;
	}

	public String getLogChannel() {
		return this.channelLog;
	}

	public String getReportChannel() {
		return this.channelReports;
	}

	@SuppressWarnings("unchecked")
	public <T extends DiscordModule> T getModule(Class<T> clazz) throws IllegalArgumentException {
		Validate.isTrue(DiscordModule.class.isAssignableFrom(clazz), clazz.getName() + " is not a DiscordModule.");
		Validate.isTrue(modules.containsKey(clazz), "Module not enabled!");
		Object object = modules.get(clazz);
		Validate.isTrue(clazz.isAssignableFrom(object.getClass()));
		return (T) object;
	}

	public void log(String message) {
		postMessage(this.getBotName(), message, getLogChannel());
	}

	public void postMessage(Message message, boolean global) {
		if (global) {
			postMessage((message.isThirdPerson() ? "* " : "") + message.getSenderName(),
					message.getDiscordMessage(), getMainChannel());
		}
		postMessage(this.getBotName(), message.getConsoleMessage(), getLogChannel());
	}

	public void postMessage(String name, String message, boolean global) {
		if (global) {
			postMessage(name, message, getMainChannel(), getLogChannel());
		} else {
			postMessage(name, message, getLogChannel());
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
		drainQueueThread.queue(new DiscordCallable(CallPriority.HIGH) {
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
						client.changeUsername(name);
					} catch (HTTP429Exception | DiscordException e) {
						// Trivial issue
					}
				}
				IMessage posted = group.sendMessage(message);
				if (channel.equals(getMainChannel()) && !name.equals(getBotName())) {
					StringBuilder builder = new StringBuilder().append("**")
							.append(toEscape.matcher(name).replaceAll("\\\\$1"));
					if (!name.startsWith("* ")) {
						builder.append(':');
					}
					builder.append("** ").append(message);
					drainQueueThread.queue(new DiscordCallable(CallPriority.MEDIUM, true) {
						@Override
						public void call() throws MissingPermissionsException, HTTP429Exception, DiscordException {
							// Editing messages causes them to use the current name.
							resetBotName();
							posted.edit(builder.toString());
						}
					});
				}
			}
		});
	}

	public void postReport(String message) {
		postMessage(this.getBotName(), message, getReportChannel());
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

	public DiscordPlayer getDiscordPlayerFor(IUser user) {
		UUID uuid = getUUIDOf(user);
		if (uuid == null) {
			return null;
		}
		Player player = PlayerLoader.getPlayer(this.getPlugin(), uuid);
		if (player instanceof DiscordPlayer) {
			return (DiscordPlayer) player;
		}
		// PlayerLoader loads a PermissiblePlayer, wrapping a wrapper would be silly.
		DiscordPlayer dplayer = new DiscordPlayer(this, user, player.getPlayer());
		PlayerLoader.modifyCachedPlayer(dplayer);
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
	public String getName() {
		return "Discord";
	}

}
