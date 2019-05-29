package com.easterlyn.module;

import com.easterlyn.Easterlyn;
import com.easterlyn.utilities.TextUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * The base class for all plugin modules, allowing separate components of the
 * plugin to be managed separately.
 *
 * @author FireNG, Jikoo
 */
public abstract class Module {

	private final Easterlyn plugin;

	private boolean enabled = false;
	private YamlConfiguration configuration;

	public Module(Easterlyn plugin) {
		this.plugin = plugin;
	}

	/**
	 * Enables the Module.
	 *
	 * @return the Module enabled
	 */
	public final Module enable() {
		this.getLogger().info("Enabling module " + this.getName());

		if (Easterlyn.areDependenciesMissing(this.getClass())) {
			return this;
		}

		try {
			this.onEnable();
			this.enabled = true;
		} catch (Exception e) {
			this.getLogger().severe("Unhandled exception in module " + this.getName() + ". Module failed to enable.");
			this.getLogger().severe(TextUtils.getTrace(e));
		}
		return this;
	}

	/**
	 * Called when the Module is enabled.
	 */
	protected abstract void onEnable();

	/**
	 * Disables the Module.
	 */
	public final void disable() {
		this.getLogger().info("Disabling module " + this.getName());
		this.saveConfig();
		try {
			this.onDisable();
			this.enabled = false;
		} catch (Exception e) {
			this.getLogger().severe("Unhandled exception in module " + this.getName() + ". Module failed to disable.");
			this.getLogger().severe(TextUtils.getTrace(e));
		}
	}

	/**
	 * Called when the Module is disabled before handlers are unassigned.
	 */
	protected abstract void onDisable();

	/**
	 * Gets whether or not the Module is enabled.
	 *
	 * @return true if the Module is enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Gets whether or not the Module needs to be enabled for the plugin to function.
	 *
	 * @return true if the Module is essential and cannot be disabled.
	 */
	public abstract boolean isRequired();

	/**
	 * Get the simple name of the Module.
	 *
	 * @return the name of this Module
	 */
	public abstract String getName();

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
	 * Gets the Easterlyn instance that loaded this Module.
	 *
	 * @return the Easterlyn
	 */
	public final Easterlyn getPlugin() {
		return this.plugin;
	}

	/**
	 * Gets the YamlConfiguration for data specific to this Module.
	 *
	 * @return the YamlConfiguration
	 */
	public final YamlConfiguration getConfig() {
		if (this.configuration == null) {
			return loadConfig();
		}
		return this.configuration;
	}

	/**
	 * Loads the YamlConfiguration for data specific to this Module.
	 *
	 * @return the YamlConfiguration
	 */
	public YamlConfiguration loadConfig() {
		File file = new File(getPlugin().getDataFolder(), this.getName().replaceAll("\\W", "") + ".yml");
		if (file.exists()) {
			this.configuration = YamlConfiguration.loadConfiguration(file);
		} else {
			this.configuration = new YamlConfiguration();
		}
		return this.configuration;
	}

	/**
	 * Saves this Module's YamlConfiguration if it has been loaded.
	 */
	public final void saveConfig() {
		if (this.configuration != null) {
			File file = new File(getPlugin().getDataFolder(), this.getName().replaceAll("\\W", "") + ".yml");
			try {
				this.configuration.save(file);
			} catch (IOException e) {
				getLogger().severe("Unhandled exception in module " + this.getName() + ". Module failed to disable.");
				e.printStackTrace();
			}
		}
	}

}
