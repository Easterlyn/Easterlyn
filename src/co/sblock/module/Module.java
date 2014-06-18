package co.sblock.module;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import co.sblock.Sblock;
import co.sblock.utilities.Log;

/**
 * The base class for all plugin modules, allowing separate components of the plugin to be managed
 * separately.
 * 
 * @author FireNG
 */
public abstract class Module {

	/* The Set of Listeners registered by this Module. */
	private Set<Listener> listeners = new HashSet<Listener>();

	/**
	 * Called when the Module is enabled.
	 */
	protected abstract void onEnable();

	/**
	 * Called when the Module is disabled before handlers are unassigned.
	 */
	protected abstract void onDisable();

	/**
	 * Register events for one or more Listeners.
	 * 
	 * @param listeners Listener[]
	 */
	protected final void registerEvents(Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getPluginManager().registerEvents(listener, Sblock.getInstance());
			this.listeners.add(listener);
		}
	}

	/**
	 * Registers all of the Commands handled in the given CommandListener.
	 * 
	 * @param listener the Listener to register.
	 */
	protected final void registerCommands(CommandListener listener) {
		Sblock.getInstance().registerCommands(listener);
	}

	/**
	 * Enables the Module.
	 * 
	 * @return the Module enabled
	 */
	public final Module enable() {
		getLogger().info("Enabling module " + getName());
		try {
			this.onEnable();
		} catch (Exception e) {
			String message = "[SblockSuite] Unhandled exception in module " + getName()
					+ ". Plugin failed to load.";
			throw new RuntimeException(message, e);
		}
		return this;
	}

	/**
	 * Disables the Module.
	 * 
	 * @return the Module disabled
	 */
	public final Module disable() {
		try {
			this.onDisable();
			for (Listener listener : listeners) {
				HandlerList.unregisterAll(listener);
			}
		} catch (Exception e) {
			String message = "Unhandled exception in module " + getName()
					+ ". Plugin failed to properly disable.";
			throw new RuntimeException(message, e);
		}
		getLogger().info("Disabled module " + getName());
		return this;
	}

	/**
	 * Gets a Logger that the plugin may use whose name is the same as this Module's class name.
	 * 
	 * @return the Log
	 */
	public final Log getLogger() {
		return Log.getLog(getName());
	}

	/**
	 * A replacement for calling the reflective functions for logging purposes. Reflective overhead
	 * is large, and there really is no excuse for throwing away memory / runtime.
	 * 
	 * @return the name of the module
	 */
	public abstract String getName();
}
