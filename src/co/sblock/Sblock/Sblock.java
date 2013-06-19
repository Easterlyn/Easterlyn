package co.sblock.Sblock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

public class Sblock extends JavaPlugin {

	Connection database;

	@Override
	public void onEnable() {
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
	}

	@Override
	public void onDisable() {
	}
}
