package co.sblock.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.chat.channel.Channel;
import co.sblock.machines.type.Machine;
import co.sblock.users.TowerData;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.Log;

/**
 * Collection of all database-related functions.
 * 
 * @author Jikoo, FireNG
 */
public class SblockData {
	/** The SblockData instance. */
	private static SblockData db;

	private static final Log logger = Log.getLog("SblockData");

	/**
	 * SblockData singleton.
	 * 
	 * @return the SblockData instance
	 */
	public static SblockData getDB() {
		if (db == null)
			db = new SblockData();
		return db;
	}

	/** The SQL Connection used by the SblockData. */
	private Connection connection;

	/**
	 * Establish connection to database and create SblockData instance.
	 * 
	 * @return true if enabled successfully
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

	/**
	 * Close Connection and set instance to null.
	 */
	public void disable() {
		try {
			connection.close();
		} catch (Exception e) {
			logger.err(e);
		}
		db = null;
		connection = null;
	}

	/**
	 * Get the Connection to the database.
	 * 
	 * @return the Connection
	 */
	protected Connection connection() {
		return connection;
	}

	/**
	 * Gets a Log for database-related events. Used to prevent confusion about
	 * the source of messages.
	 * 
	 * @return the Log
	 */
	public static Log getLogger() {
		return logger;
	}

	/**
	 * Initiate user data saving for a Player by name.
	 * 
	 * @param name the name of the Player
	 */
	public void saveUserData(UUID userID) {
		PlayerData.saveUserData(userID);
	}

	/**
	 * Initiate loading of a Player's stored data.
	 * 
	 * @param name the name of the Payer to load data for
	 */
	public User loadUserData(UUID userID) {
		PlayerData.loadUserData(userID);
		return UserManager.getUserManager().addUser(userID);
	}

	public void startOfflineLookup(CommandSender sender, String name) {
		PlayerData.startOfflineLookup(sender, name);
	}

	/**
	 * Delete specified Player's data from database.
	 * 
	 * @param name the name of the Player whose data is to be deleted
	 */
	public void deleteUser(String name) {
		PlayerData.deleteUser(name);
	}

	/**
	 * Save Channel data to database.
	 * 
	 * @param c the Channel to save data for
	 */
	public void saveChannelData(Channel c) {
		ChatChannels.saveChannelData(c);
	}

	/**
	 * Creates and loads all Channels from saved data.
	 */
	public void loadAllChannelData() {
		ChatChannels.loadAllChannelData();
	}

	/**
	 * Delete a Channel by name.
	 * 
	 * @param channelName the name of the Channel to delete
	 */
	public void deleteChannel(String channelName) {
		ChatChannels.deleteChannel(channelName);
	}

	/**
	 * Save Machine data to database.
	 * 
	 * @param m the Machine to save data for
	 */
	public void saveMachine(Machine m) {
		Machines.saveMachine(m);
	}

	/**
	 * Delete a specified Machine's data from database.
	 * 
	 * @param m the Machine to delete data of
	 */
	public void deleteMachine(Machine m) {
		Machines.deleteMachine(m);
	}

	/**
	 * Creates and loads all Machines from saved data.
	 */
	public void loadAllMachines() {
		Machines.loadAllMachines();
	}

	/**
	 * @deprecated Make a custom call to the database. For testing purposes
	 *             only! Make a new method for new features.
	 * @param MySQLStatement the call to make
	 * @param resultExpected true if a ResultSet is expected
	 * 
	 * @return the ResultSet generated, if any.
	 */
	public ResultSet makeCustomCall(String MySQLStatement) {
		try {
			return connection.prepareStatement(MySQLStatement).executeQuery();
		} catch (SQLException e) {
			logger.err(e);
			return null;
		}
	}

	/**
	 * Fills out TowerData from saved data.
	 */
	public void loadTowerData() {
		TowerLocs.loadTowerData();
	}

	/**
	 * Save all TowerData.
	 * 
	 * @param towers the TowerData to save
	 */
	public void saveTowerData(TowerData towers) {
		TowerLocs.saveTowerData(towers);
	}

	/**
	 * Get a User's name by the IP they last connected with.
	 * 
	 * @param hostAddress the IP to look up
	 * 
	 * @return the name of the User, "Player" if invalid
	 */
	public String getUserFromIP(String hostAddress) {
		return PlayerData.getUserFromIP(hostAddress);
	}

	/**
	 * Get the reason a User was banned.
	 * 
	 * @param user the name of UUID of the banned User
	 * @param ip the IP of the banned User
	 * 
	 * @return the ban reason
	 */
	public String getBanReason(String user, String ip) {
		return BannedPlayers.getBanReason(user, ip);
	}

	/**
	 * Add a ban and reason to a User.
	 * 
	 * @param target the User to add a ban for
	 * @param reason the reason the User was banned
	 */
	public void addBan(User target, String reason) {
		BannedPlayers.addBan(target, reason);
	}

	/**
	 * Remove a ban by name, IP, or UUID.
	 * 
	 * @param target the name, IP, or UUID to unban
	 */
	public void removeBan(String target) {
		BannedPlayers.deleteBans(target);
	}
}
