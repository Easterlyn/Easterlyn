package co.sblock.module;

import co.sblock.utilities.Log;

/**
 * The base class for all plugin modules, allowing separate components of the
 * plugin to be managed separately.
 * 
 * @author FireNG
 */
public abstract class Module {

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
