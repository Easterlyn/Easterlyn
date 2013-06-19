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
		Sblock s = Sblock.getInstance();
		try {
			Class.forName("org.postgresql.Driver");
			database = DriverManager.getConnection(s.getConfig()
					.getString("host") + ":" + s.getConfig().getString("port"),
					s.getConfig().getString("username"),
					s.getConfig().getString("password"));
		} catch (ClassNotFoundException e) {
			// if we can't connect to the database, we're pretty much done here.
			s.getPluginLoader().disablePlugin(s);
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			s.getPluginLoader().disablePlugin(s);
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
