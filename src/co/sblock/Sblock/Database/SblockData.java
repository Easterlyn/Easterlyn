package co.sblock.Sblock.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.command.CommandSender;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.TowerData;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Log;

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
		logger.fine("Connecting to database.");
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
	public void saveUserData(String name) {
		PlayerData.saveUserData(name);
	}

	/**
	 * Initiate loading of a Player's stored data.
	 * 
	 * @param name the name of the Payer to load data for
	 */
	public ChatUser loadUserData(String name) {
		UserManager.getUserManager().addUser(name);
		ChatUser u = ChatUserManager.getUserManager().addUser(name);
		PlayerData.loadUserData(name);
		return u;
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
	 * Get a SblockUser's name by the IP they last connected with.
	 * 
	 * @param hostAddress the IP to look up
	 * 
	 * @return the name of the SblockUser, "Player" if invalid
	 */
	public String getUserFromIP(String hostAddress) {
		return PlayerData.getUserFromIP(hostAddress);
	}

	/**
	 * Get the reason a SblockUser was banned.
	 * 
	 * @param name the name of the banned SblockUser
	 * @param ip the IP of the banned SblockUser
	 * 
	 * @return the ban reason
	 */
	public String getBanReason(String name, String ip) {
		return BannedPlayers.getBanReason(name, ip);
	}

	/**
	 * Add a ban and reason to a SblockUser.
	 * 
	 * @param target the SblockUser to add a ban for
	 * @param reason the reason the SblockUser was banned
	 */
	public void addBan(SblockUser target, String reason) {
		BannedPlayers.addBan(target, reason);
	}

	/**
	 * Remove a ban by name or IP.
	 * 
	 * @param target the name or IP to unban
	 */
	public void removeBan(String target) {
		BannedPlayers.deleteBans(target);
	}
}
