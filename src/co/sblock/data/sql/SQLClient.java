package co.sblock.data.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.tmathmeyer.jadis.async.Promise;

import co.sblock.Sblock;
import co.sblock.chat.channel.Channel;
import co.sblock.data.ChatChannels;
import co.sblock.data.Machines;
import co.sblock.data.PlayerData;
import co.sblock.data.SblockData;
import co.sblock.machines.type.Machine;
import co.sblock.utilities.Log;

/**
 * SQL Database implementation
 * 
 * @author Jikoo, FireNG, tmathmeyer
 */
public class SQLClient extends SblockData {

	/* The SQL Connection used by the SblockData. */
	private Connection connection;
	private final Log logger = Log.getLog("SblockData-SQL");

	@Override
	/*
	 * @see co.sblock.data.SblockData#enable()
	 */
	public boolean enable() {
		logger.info("Enabling SblockData");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ Sblock.getInstance().getConfig().getString("host") + ":"
					+ Sblock.getInstance().getConfig().getString("port") + "/"
					+ Sblock.getInstance().getConfig().getString("database"),
					Sblock.getInstance().getConfig().getString("username"),
					Sblock.getInstance().getConfig().getString("password"));
			logger.fine("Connection established.");
		} catch (ClassNotFoundException e) {
			logger.severe("Database driver not found. Plugin functionality will be limited.");
			return false;
		} catch (SQLException e) {
			logger.severe("Connection error. Plugin functionality will be limited.");
			logger.criticalErr(e);
			return false;
		} catch (NullPointerException e) {
			logger.severe("Invalid config! Required strings: host, port, database, username, password.");
		}

		logger.fine("Database enabled");
		return true;
	}

	@Override
	public void disable() {
		try {
			connection.close();
		} catch (Exception e) {
			logger.err(e);
		}
		connection = null;
	}

	@Override
	protected Connection connection() {
		try {
			if (connection == null || connection.isClosed()) {
				enable();
			}
		} catch (SQLException e) {
			// Yep, we're screwed.
			enable();
		}
		return connection;
	}

	@Override
	public void saveUserData(UUID userID) {
		PlayerData.saveUserData(userID);
	}

	@Override
	public void loadUserData(UUID userID) {
		PlayerData.loadUserData(userID);
	}

	public void startOfflineLookup(CommandSender sender, String name) {
		PlayerData.startOfflineLookup(sender, name);
	}

	@Override
	public void deleteUser(UUID userID) {
		PlayerData.deleteUser(userID);
	}

	@Override
	public void saveChannelData(Channel c) {
		ChatChannels.saveChannelData(c);
	}

	@Override
	public void loadAllChannelData() {
		ChatChannels.loadAllChannelData();
	}

	@Override
	public void deleteChannel(String channelName) {
		ChatChannels.deleteChannel(channelName);
	}

	@Override
	public void saveMachine(Machine m) {
		Machines.saveMachine(m);
	}

	@Override
	public void deleteMachine(Machine m) {
		Machines.deleteMachine(m);
	}

	@Override
	public void loadAllMachines() {
		Machines.loadAllMachines();
	}

	@Override
	public void getUserFromIP(String hostAddress, Promise<String> executor) {
		executor.getObject(PlayerData.getUserFromIP(hostAddress), null);
	}

	@Override
	public Log getLogger() {
		return logger;
	}

	@Override
	public void enterFinalizeMode() {
		// dont do anything
	}
}
