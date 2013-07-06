/**
 * 
 */
package co.sblock.Sblock;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * The base class for all plugin modules, allowing separate components of the
 * plugin to be managed separately.
 * 
 * @author FireNG
 * 
 */
public abstract class Module {
	private Set<Listener> listeners = new HashSet<Listener>();
	private Set<String> executors = new HashSet<String>();

	protected abstract void onEnable();

	protected abstract void onDisable();

	protected final void registerEvents(Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getPluginManager().registerEvents(listener, Sblock.getInstance());
			this.listeners.add(listener);
		}
	}

	/**
	 * Registers all of the commands handled in the given CommandListener class.
	 * 
	 * @param listener
	 *            Listener class to register.
	 */
	protected final void registerCommands(CommandListener listener) {
		for (Class<?> clazz : listener.getClass().getDeclaredClasses()) {
			if (CommandExecutor.class.isAssignableFrom(clazz) && clazz.getAnnotation(CommandHandler.class) != null) {
				String commandName = clazz.getAnnotation(CommandHandler.class).name();
				try {
					CommandExecutor newExecutor = (CommandExecutor) clazz.newInstance();
					Sblock.getInstance().getCommand(commandName).setExecutor(newExecutor);
					this.executors.add(commandName);
				} catch (NullPointerException e) {
					this.getLogger().warning("The command " + commandName +
							"is not defined in the plugin.yml file, and will not work until defined.");
				}
                catch (InstantiationException e)
                {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
				//this.getLogger().severe("Command handler " + clazz.getSimpleName() + " in module " + this.getClass().getSimpleName() + " is invalid.");
			}
		}
	}

	/**
	 * Enables the module.
	 */
	public final void enable() {
		try {
			this.onEnable();
		} catch (Exception e) {
			throw new RuntimeException("Unhandled exception in module " + this.getClass().getSimpleName() + ". Plugin failed to load.", e);
		}
		this.getLogger().info("Loaded module " + this.getClass().getSimpleName());
	}

	/**
	 * Disables the module.
	 */
	public final void disable() {

		try {
			for (String entry : this.executors) {
				Sblock.getInstance().getCommand(entry).setExecutor(null);
			}
			for (Listener listener : listeners) {
				HandlerList.unregisterAll(listener);
			}
			this.onDisable();
		} catch (Exception e) {
			throw new RuntimeException("Unhandled exception in module " + this.getClass().getSimpleName() + ". Plugin failed to properly disable.", e);
		}
		this.getLogger().info("Disabled module " + this.getClass().getSimpleName());
		

	}

	/**
	 * @return a Logger object that the plugin may use, whose name is the same
	 *         as this module's class name.
	 */
	public final Logger getLogger() {
		return Logger.getLogger(this.getClass().getSimpleName());
	}
}
