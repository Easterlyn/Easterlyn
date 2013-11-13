package co.sblock.Sblock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Database.DBManager;
import co.sblock.Sblock.Events.EventModule;
import co.sblock.Sblock.Machines.MachineModule;
import co.sblock.Sblock.SblockEffects.EffectsModule;
import co.sblock.Sblock.UserData.UserDataModule;
import co.sblock.Sblock.Utilities.Sblogger;
//import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Counter.CounterModule;
import co.sblock.Sblock.Utilities.MeteorMod.MeteorMod;

import com.google.common.base.Joiner;

/**
 * <code>Sblock</code> is the base of Sblock.co's custom plugin. All features
 * handled by smaller <code>Module</code>s.
 * 
 * @author Jikoo, FireNG, Dublek
 */
public class Sblock extends JavaPlugin {

	/** The <code>Sblock</code> instance. */
	private static Sblock instance;

	/** The <code>Set</code> of <code>Module</code>s enabled. */
	private Set<Module> modules;

	/**
	 * The <code>Map</code> of commands currently being managed and their
	 * respective <code>Method</code>.
	 */
	private Map<String, Method> commandHandlers;

	/** The <code>Map</code> of registered <code>CommandListeners</code>. */
	private Map<Class<? extends CommandListener>, CommandListener> listenerInstances;

	/**
	 * Get the current instance of the <code>Sblock</code> plugin.
	 * 
	 * @return the <code>Sblock</code> instance
	 */
	public static Sblock getInstance() {
		return instance;
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
		instance = this;
		this.modules = new HashSet<Module>();
		this.commandHandlers = new HashMap<String, Method>();
		this.listenerInstances = new HashMap<Class<? extends CommandListener>, CommandListener>();
		saveDefaultConfig();
		DBManager.getDBM().enable();
		
		modules.add(new UserDataModule().enable());
		modules.add(new ChatModule().enable());
		modules.add(new EventModule().enable());
		modules.add(new MeteorMod().enable());
		modules.add(new EffectsModule().enable());
//		modules.add(new Captcha().enable());
		modules.add(new MachineModule().enable());
		modules.add(new CounterModule().enable());
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		instance = null;
		for (Module module : this.modules) {
			module.disable();
		}
		this.unregisterAllCommands();
		HandlerList.unregisterAll(this);
		DBManager.getDBM().disable();
	}

	/**
	 * Register all commands for a CommandListener.
	 * 
	 * @param listener
	 *            the CommandListener to register commands for
	 */
	public void registerCommands(CommandListener listener) {
		for (Method method : listener.getClass().getMethods()) {
			if (this.commandHandlers.containsKey(method))
				throw new Error("Duplicate handlers for command " + method + " found in "
						+ this.commandHandlers.get(method.getName()).getDeclaringClass().getName()
						+ " and " + method.getDeclaringClass().getName());
			if (method.getAnnotation(SblockCommand.class) != null
					&& method.getParameterTypes().length > 0
					&& CommandSender.class.isAssignableFrom(method.getParameterTypes()[0]))
				this.commandHandlers.put(method.getName(), method);
		}
		this.listenerInstances.put(listener.getClass(), listener);
	}

	/**
	 * Unregister all registered commands.
	 */
	public void unregisterAllCommands() {
		for (Method m : this.commandHandlers.values()) {
			Bukkit.getPluginCommand(m.getName()).setExecutor(null);
		}
	}

	/**
	 * Handles all commands registered for Sblock.
	 * 
	 * @see org.bukkit.command.CommandExecutor#onCommand(CommandSender, Command,
	 *      String, String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!this.commandHandlers.containsKey(label)) {
			this.getLogger().warning(
					"Command /" + label + " has no associated handler.");
			sender.sendMessage(ChatColor.RED
					+ "An internal error has occurred. Please notify a member of staff of this issue as soon as possible.");
			return true;
		} else {
			Method handlerMethod = this.commandHandlers.get(label);
			Object[] params = new Object[handlerMethod.getParameterTypes().length];
			if (sender instanceof ConsoleCommandSender
					&& !handlerMethod.getAnnotation(SblockCommand.class)
							.consoleFriendly()) {
				sender.sendMessage("You must be a player to use this command.");
				return true;
			}
			params[0] = sender;
			if (handlerMethod.getAnnotation(SblockCommand.class).mergeLast()
					&& params.length - 1 <= args.length) {
				System.arraycopy(args, 0, params, 1, params.length - 2);
				params[params.length - 1] = Joiner.on(" ")
						.join(Arrays.copyOfRange(args, params.length - 2,
								args.length));
			} else if (params.length - 1 == args.length) {
				System.arraycopy(args, 0, params, 1, params.length - 1);
			} else
				// Not the right amount of arguments, GTFO
				return false;
			try {
				return (Boolean) handlerMethod.invoke(this.listenerInstances
						.get(handlerMethod.getDeclaringClass()), params);
			} catch (IllegalAccessException | InvocationTargetException e) {
				Sblogger.err(e);
			}
		}
		return false;
	}
}
