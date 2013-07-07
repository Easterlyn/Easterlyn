package co.sblock.Sblock;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import co.sblock.Sblock.PlayerData.PlayerDataModule;

public class Sblock extends JavaPlugin {

	private static Sblock instance;
	
	private Set<Module> modules;

	public static Sblock getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		this.modules = new HashSet<Module>();
		saveDefaultConfig();
		if (DatabaseManager.getDatabaseManager().enable())
			this.getLogger().info("Connected");
		else
			this.getLogger().info("Unable to connect");
		
		modules.add(new PlayerDataModule().enable());

		/*
		 * Ok, so here.. This being the framework behind all the sub-plugin
		 * (module, whatever) handlers..
		 * 
		 * Each module main class needs an enable() and disable() that will
		 * register and unregister its own commands and event handler(s)
		 * 
		 * public void enable() {
		 * getCommand("moduleCommand").setExecutor(moduleCommandExecutor);
		 * getServer().getPluginManager().registerEvents(moduleListener,
		 * Sblock.getInstance()); }
		 */

	}

	@Override
	public void onDisable() {
		instance = null;
		try {
			DatabaseManager.getDatabaseManager().disable();
		} catch (NullPointerException npe) {
			// Caused by any load failures; modules may not be initialized.
		}
		for(Module module : this.modules)
		    module.disable();
		// Just in case we missed something (somehow)
		HandlerList.unregisterAll(this);
	}
}
