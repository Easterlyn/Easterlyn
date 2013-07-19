package co.sblock.Sblock;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
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

	/**
	 * Called when the module is enabled.
	 */
	protected abstract void onEnable();
	
	/**
	 * Called when the module is disabled before handlers are unassigned. 
	 */
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
	    Sblock.getInstance().registerCommands(listener);
	}

	/**
	 * Enables the module.
	 */
	public final Module enable() {
		try {
			this.onEnable();
		} catch (Exception e) {
			throw new RuntimeException("Unhandled exception in module " + this.getClass().getSimpleName() + ". Plugin failed to load.", e);
		}
		this.getLogger().info("Loaded module " + this.getClass().getSimpleName());
		return this;
	}

	/**
	 * Disables the module.
	 */
	public final Module disable() {

		try {
			this.onDisable();
			for (Listener listener : listeners) {
				HandlerList.unregisterAll(listener);
			}
		} catch (Exception e) {
			throw new RuntimeException("Unhandled exception in module " + this.getClass().getSimpleName() + ". Plugin failed to properly disable.", e);
		}
		this.getLogger().info("Disabled module " + this.getClass().getSimpleName());
		return this;
	}

	/**
	 * @return a Logger object that the plugin may use, whose name is the same
	 *         as this module's class name.
	 */
	public final Logger getLogger() {
		return Logger.getLogger(this.getClass().getSimpleName());
	}
}
