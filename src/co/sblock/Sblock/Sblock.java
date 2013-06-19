package co.sblock.Sblock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Sblock extends JavaPlugin {

	private static Sblock instance;

	public static Sblock getInstance() {
		return instance;
	}

	Connection database;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		
		try {
			Class.forName("org.postgresql.Driver");
			database = DriverManager.getConnection(getConfig()
					.getString("host") + ":" + getConfig().getString("port"),
					getConfig().getString("username"),
					getConfig().getString("password"));
		} catch (ClassNotFoundException e) {
			// if we can't connect to the database, we're pretty much done here.
			getPluginLoader().disablePlugin(this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			database.close();
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
