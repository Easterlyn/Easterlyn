package co.sblock.Sblock;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import co.sblock.Sblock.Chat.SblockChat;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.SblockEffects.SblockEffects;
import co.sblock.Sblock.UserData.SblockUsers;
import co.sblock.Sblock.Utilities.Log;
import co.sblock.Sblock.Utilities.Counter.CounterModule;
import co.sblock.Sblock.Utilities.MeteorMod.MeteorMod;
import co.sblock.Sblock.Utilities.RawMessages.RawAnnouncer;

/**
 * Sblock is the base of Sblock.co's custom plugin. All features are handled by
 * smaller modules.
 * 
 * @author Jikoo, FireNG, Dublek
 */
public class Sblock extends JavaPlugin {

	/** Sblock's Log */
	private static final Log logger = new Log("Sblock", null);

	/** The Sblock instance. */
	private static Sblock instance;

	/** The Set of Modules enabled. */
	private Set<Module> modules;

	/** The Map of commands currently being managed and their respective Method. */
	private Map<String, Method> commandHandlers;

	/** The Map of registered CommandListeners. */
	private Map<Class<? extends CommandListener>, CommandListener> listenerInstances;

	/** The CommandMap used to register commands for Modules. */
	private CommandMap cmdMap;

	/**
	 * Get the current instance of the Sblock plugin.
	 * 
	 * @return the Sblock instance
	 */
	public static Sblock getInstance() {
		return instance;
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
		if (Bukkit.getServer() instanceof org.bukkit.craftbukkit.v1_7_R1.CraftServer) {
			try {
				Field f = org.bukkit.craftbukkit.v1_7_R1.CraftServer.class.getDeclaredField("commandMap");
				f.setAccessible(true);
				cmdMap = (CommandMap) f.get(Bukkit.getServer());
			} catch (IllegalArgumentException | IllegalAccessException
					| NoSuchFieldException | SecurityException e) {
				Log.criticalErr(e);
			}
		} else {
			getLog().severe("Invalid server version, Sblock commands will fail to register.");
		}
		instance = this;
		this.modules = new HashSet<Module>();
		this.commandHandlers = new HashMap<String, Method>();
		this.listenerInstances = new HashMap<Class<? extends CommandListener>, CommandListener>();
		saveDefaultConfig();
		SblockData.getDB().enable();
		
		modules.add(new SblockChat().enable());
		modules.add(new SblockUsers().enable());
		modules.add(new SblockEvents().enable());
		modules.add(new SblockEffects().enable());
		modules.add(new SblockMachines().enable());
//		modules.add(new Captcha().enable());
		modules.add(new CounterModule().enable());
		modules.add(new MeteorMod().enable());
		modules.add(new RawAnnouncer().enable());
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		this.unregisterAllCommands();
		HandlerList.unregisterAll(this);
		for (Module module : this.modules) {
			module.disable();
		}
		SblockData.getDB().disable();
		instance = null;
	}

	/**
	 * Register all commands for a CommandListener.
	 * 
	 * @param listener the CommandListener to register commands for
	 */
	public void registerCommands(CommandListener listener) {
		for (Method method : listener.getClass().getMethods()) {
			if (this.commandHandlers.containsKey(method))
				throw new Error("Duplicate handlers for command " + method + " found in "
						+ this.commandHandlers.get(method.getName()).getDeclaringClass().getName()
						+ " and " + listener.getClass().getName());
			if (method.getAnnotation(SblockCommand.class) != null
					&& method.getParameterTypes().length > 1
					&& CommandSender.class.isAssignableFrom(method.getParameterTypes()[0])
					&& String[].class.isAssignableFrom(method.getParameterTypes()[1])) {
				this.commandHandlers.put(method.getName(), method);
				cmdMap.register(this.getDescription().getName(), createCommand(method));
			}
		}
		this.listenerInstances.put(listener.getClass(), listener);
	}

	private CustomCommand createCommand(Method m) {
		CustomCommand cmd = new CustomCommand(m.getName());
		cmd.setDescription(m.getAnnotation(SblockCommand.class).description());
		cmd.setUsage(m.getAnnotation(SblockCommand.class).usage());
		cmd.setPermission(m.getAnnotation(SblockCommand.class).permission());
		cmd.setPermissionMessage(ChatMsgs.permissionDenied());
		return cmd;
	}

	/**
	 * Unregister all registered commands.
	 */
	public void unregisterAllCommands() {
		for (Method m : this.commandHandlers.values()) {
			try {
				Bukkit.getPluginCommand(m.getName()).setExecutor(null);
			} catch (NullPointerException e) {
				getLog().fine("Command " + m.getName() + " was registered interally.");
			}
		}
	}

	/**
	 * Passes all registered commands to the CommandListener that registered them.
	 * 
	 * @see org.bukkit.command.CommandExecutor#onCommand(CommandSender, Command,
	 *      String, String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!this.commandHandlers.containsKey(label)) {
			this.getLogger().warning( "Command /" + label + " has no associated handler.");
			sender.sendMessage(ChatColor.RED
					+ "An internal error has occurred. Please notify a member of staff of this issue as soon as possible.");
			return true;
		} else {
			Method handlerMethod = this.commandHandlers.get(label);
			if (sender instanceof ConsoleCommandSender
					&& !handlerMethod.getAnnotation(SblockCommand.class).consoleFriendly()) {
				sender.sendMessage("This command cannot be issued from the console.");
				return true;
			}
			try {
				if (!(Boolean) handlerMethod.invoke(this.listenerInstances
						.get(handlerMethod.getDeclaringClass()), sender, args)) {
					sender.sendMessage(command.getUsage());
				}
				return true;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				Log.err(e);
			}
		}
		return false;
	}

	public static final Log getLog() {
		return logger;
	}
}
