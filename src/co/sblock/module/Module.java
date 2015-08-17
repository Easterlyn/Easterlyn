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
	 * Enables the Module.
	 * 
	 * @return the Module enabled
	 */
	public final Module enable() {
		this.getLogger().info("Enabling module " + this.getModuleName());

		if (!Sblock.areDependenciesPresent(getClass())) {
			return this;
		}

		try {
			this.onEnable();
			enabled = true;
		} catch (Exception e) {
			getLogger().severe("Unhandled exception in module " + this.getModuleName() + ". Module failed to enable.");
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
		this.getLogger().info("Disabled module " + this.getModuleName());
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
	 * @return the Log
	 */
	public final Logger getLogger() {
		return Logger.getLogger(getModuleName());
	}
}
