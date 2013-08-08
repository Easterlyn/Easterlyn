package co.sblock.Sblock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.UserData.UserDataModule;

import com.google.common.base.Joiner;

public class Sblock extends JavaPlugin {

	private static Sblock instance;

	private Set<Module> modules;
	private Map<String, Method> commandHandlers;
	private Map<Class<? extends CommandListener>, CommandListener> listenerInstances;

	public static Sblock getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		this.modules = new HashSet<Module>();
		this.commandHandlers = new HashMap<String, Method>();
		this.listenerInstances = new HashMap<Class<? extends CommandListener>, CommandListener>();
		saveDefaultConfig();
		DatabaseManager.getDatabaseManager().enable();
		
		modules.add(new UserDataModule().enable());
		modules.add(new ChatModule().enable());
	}

	@Override
	public void onDisable() {
		instance = null;
		for (Module module : this.modules) {
			module.disable();
		}
		try {
			DatabaseManager.getDatabaseManager().disable();
		} catch (NullPointerException npe) {
			// Caused by any load failures; modules may not be initialized.
		}
	}

	public void registerCommands(CommandListener listener) {
		for (Method method : listener.getClass().getMethods()) {
			if (this.commandHandlers.containsKey(method))
				throw new Error("Duplicate handlers for command " + method + " found in " + this.commandHandlers.get(method.getName()).getDeclaringClass().getName() + " and " + method.getDeclaringClass().getName());
			if (method.getAnnotation(SblockCommand.class) != null
					&& method.getParameterTypes().length > 0
					&& CommandSender.class.isAssignableFrom(method.getParameterTypes()[0]))
				this.commandHandlers.put(method.getName(), method);
		}
		this.listenerInstances.put(listener.getClass(), listener);
	}

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
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
