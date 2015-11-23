package co.sblock.module;

import java.util.logging.Logger;

import co.sblock.Sblock;
import co.sblock.utilities.RegexUtils;

/**
 * The base class for all plugin modules, allowing separate components of the
 * plugin to be managed separately.
 * 
 * @author FireNG, Jikoo
 */
public abstract class Module {

	private boolean enabled = false;
	private final Sblock plugin;

	public Module(Sblock plugin) {
		this.plugin = plugin;
		getLogger().info("Initializing module " + getName());
	}

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
	public abstract String getName();

	/**
	 * Enables the Module.
	 * 
	 * @return the Module enabled
	 */
	public final Module enable() {
		this.getLogger().info("Enabling module " + this.getName());

		if (!Sblock.areDependenciesPresent(getClass())) {
			return this;
		}

		try {
			this.onEnable();
			enabled = true;
		} catch (Exception e) {
			getLogger().severe("Unhandled exception in module " + this.getName() + ". Module failed to enable.");
			getLogger().severe(RegexUtils.getTrace(e));
		}
		return this;
	}

	/**
	 * Disables the Module.
	 * 
	 * @return the Module disabled
	 */
	public final Module disable() {
		this.getLogger().info("Disabled module " + this.getName());
		try {
			this.onDisable();
			enabled = false;
		} catch (Exception e) {
			getLogger().severe("[SblockSuite] Unhandled exception in module " + this.getClass().getSimpleName() + ". Module failed to disable.");
			getLogger().severe(RegexUtils.getTrace(e));
		}
		return this;
	}

	/**
	 * Gets whether or not the Module is enabled.
	 * 
	 * @return true if the Module is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Gets a Logger that the plugin may use whose name is the same as this
	 * Module's class name.
	 * 
	 * @return the Logger
	 */
	public final Logger getLogger() {
		return Logger.getLogger(getName());
	}

	/**
	 * Gets the Sblock instance that loaded this Module.
	 * 
	 * @return the Sblock
	 */
	public final Sblock getPlugin() {
		return this.plugin;
	}
}
