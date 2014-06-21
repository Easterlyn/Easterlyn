package co.sblock.module;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import co.sblock.Sblock;
import co.sblock.utilities.Log;

/**
 * The base class for all plugin modules, allowing separate components of the
 * plugin to be managed separately.
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
	 * To be used instead of the reflective class.getSimpleName() method
	 * @return the name of this module
	 */
	protected abstract String getModuleName();

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
		this.getLogger().info("Enabling module " + this.getModuleName());
		try {
			this.onEnable();
		} catch (Exception e) {
			throw new RuntimeException("[SblockSuite] Unhandled exception in module "
					+ this.getClass().getSimpleName() + ". Plugin failed to load.", e);
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
			throw new RuntimeException("Unhandled exception in module "
					+ this.getClass().getSimpleName() + ". Plugin failed to properly disable.", e);
		}
		this.getLogger().info("Disabled module " + this.getModuleName());
		return this;
	}

	/**
	 * Gets a Logger that the plugin may use whose name is the same as this
	 * Module's class name.
	 * 
	 * @return the Log
	 */
	public final Log getLogger() {
		return Log.getLog(getModuleName());
	}
}
