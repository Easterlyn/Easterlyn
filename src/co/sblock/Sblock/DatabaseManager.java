package co.sblock.Sblock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Collection of all database-related functions
 * 
 * @author Jikoo
 *
 */
public class DatabaseManager {

	private static DatabaseManager dbm;

	public static DatabaseManager getDatabaseManager() {
		if (dbm == null) dbm = new DatabaseManager();
		return dbm;
	}

	private Connection database;

	public boolean enable() {
		Sblock plugin = Sblock.getInstance();
		try {
			Class.forName("org.postgresql.Driver");
			database = DriverManager.getConnection(plugin.getConfig()
					.getString("host") + ":" + plugin.getConfig().getString("port"),
					plugin.getConfig().getString("username"),
					plugin.getConfig().getString("password"));
		} catch (ClassNotFoundException e) {
			// if we can't connect to the database, we're pretty much done here.
			plugin.getLogger().severe("The database driver was not found. Plugin functionality will be limited.");
			return false;
		} catch (SQLException e) {
			plugin.getLogger().severe("An error occurred while connecting to the database. Plugin functionality will be limited.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void disable() {
		try {
			database.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dbm = null;
		database = null;
	}
}
