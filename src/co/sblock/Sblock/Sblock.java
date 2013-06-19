package co.sblock.Sblock;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Sblock extends JavaPlugin {

	private static Sblock instance;

	public static Sblock getInstance() {
		return instance;
	}


	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		
		if (!DatabaseManager.getDatabaseManager().enable()) return;
		/* 
		 * Ok, so here.. This being the framework behind all the
		 * sub-plugin (module, whatever) handlers..
		 * 
		 * Each module main class needs an enable() and disable() that will 
		 * register and unregister its own commands and event handler(s)
		 * 
		 * public void enable() {
		 * getCommand("moduleCommand").setExecutor(moduleCommandExecutor);
		 * getServer().getPluginManager().registerEvents(moduleListener, Sblock.getInstance());
		 * }
		 */
	}

	@Override
	public void onDisable() {
		instance = null;
		try {
			DatabaseManager.getDatabaseManager().disable();
			/* 
			 * For each and every module:
			 * public void disable() {
			 * getCommand("moduleCommand").setExecutor(null);
			 * HandlerList.unregisterAll(this);
			 * }
			 * module.disable();
			 * module = null;
			 */
		} catch (NullPointerException npe) {
			// Caused by the ClassNotFoundException above;
			// Connection and modules would not be initialized
		}
		HandlerList.unregisterAll(this);
	}
}
