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
		getLogger().fine("Connecting to database.");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ Sblock.getInstance().getConfig().getString("host") + ":"
					+ Sblock.getInstance().getConfig().getString("port") + "/"
					+ Sblock.getInstance().getConfig().getString("database"),
					Sblock.getInstance().getConfig().getString("username"),
					Sblock.getInstance().getConfig().getString("password"));
			getLogger().fine("Connection established.");
		} catch (ClassNotFoundException e) {
			getLogger().severe("Database driver not found. Plugin functionality will be limited.");
			return false;
		} catch (SQLException e) {
			getLogger().severe("Connection error. Plugin functionality will be limited.");
			Log.criticalErr(e);
			return false;
		} catch (NullPointerException e) {
			getLogger().severe("Invalid config! Required strings: host, port, database, username, password.");
		}

		getLogger().fine("Database enabled");
		return true;
	}

	/**
	 * Close <code>Connection</code> and set instance to null.
	 */
	public void disable() {
		try {
			connection.close();
		} catch (Exception e) {
			Log.err(e);
		}
		db = null;
		connection = null;
	}

	protected Connection connection() {
		return connection;
	}

	public final Log getLogger() {
		return new Log("SblockData", null);
	}

	/**
	 * Initiate user data saving for a <code>Player</code> by name.
	 * 
	 * @param name
	 *            the name of the <code>Player</code>
	 */
	public void saveUserData(String name) {
		PlayerData.saveUserData(name);
	}

	/**
	 * Initiate loading of a <code>Player</code>'s stored data.
	 * 
	 * @param name
	 *            the name of the <code>Payer</code> to load data for
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
	 * Delete specified <code>Player</code>'s data from database.
	 * 
	 * @param name
	 *            the name of the <code>Player</code> whose data is to be deleted
	 */
	public void deleteUser(String name) {
		PlayerData.deleteUser(name);
	}

	/**
	 * Save <code>Channel</code> data to database.
	 * 
	 * @param c
	 *            the <code>Channel</code> to save data for
	 */
	public void saveChannelData(Channel c) {
		ChatChannels.saveChannelData(c);
	}

	/**
	 * Creates and loads all <code>Channel</code>s from saved data.
	 */
	public void loadAllChannelData() {
		ChatChannels.loadAllChannelData();
	}

	/**
	 * Delete a <code>Channel</code> by name.
	 * 
	 * @param channelName
	 *            the name of the <code>Channel</code> to delete
	 */
	public void deleteChannel(String channelName) {
		ChatChannels.deleteChannel(channelName);
	}

	/**
	 * Save <code>Machine</code> data to database.
	 * 
	 * @param m
	 *            the <code>Machine</code> to save data for
	 */
	public void saveMachine(Machine m) {
		Machines.saveMachine(m);
	}

	/**
	 * Delete a specified <code>Machine</code>'s data from database.
	 * 
	 * @param m
	 *            the <code>Machine</code> to delete data of
	 */
	public void deleteMachine(Machine m) {
		Machines.deleteMachine(m);
	}

	/**
	 * Creates and loads all <code>Machine</code>s from saved data.
	 */
	public void loadAllMachines() {
		Machines.loadAllMachines();
	}

	/**
	 * @deprecated Make a custom call to the database. For testing purposes
	 *             only! Make a new method for new features.
	 * @param MySQLStatement
	 *            the call to make
	 * @param resultExpected
	 *            true if a <code>ResultSet</code> is expected
	 * @return the <code>ResultSet</code> generated, if any.
	 */
	public ResultSet makeCustomCall(String MySQLStatement) {
		try {
			return connection.prepareStatement(MySQLStatement).executeQuery();
		} catch (SQLException e) {
			Log.err(e);
			return null;
		}
	}

	/**
	 * Fills out <code>TowerData</code> from saved data.
	 */
	public void loadTowerData() {
		TowerLocs.loadTowerData();
	}

	/**
	 * Save all <code>TowerData</code>.
	 * @param towers the <code>TowerData</code> to save
	 */
	public void saveTowerData(TowerData towers) {
		TowerLocs.saveTowerData(towers);
	}

	/**
	 * Get a <code>SblockUser</code>'s name by the IP they last connected with.
	 * 
	 * @param hostAddress
	 *            the IP to look up
	 * @return the name of the <code>SblockUser</code>, "Player" if invalid
	 */
	public String getUserFromIP(String hostAddress) {
		return PlayerData.getUserFromIP(hostAddress);
	}

	/**
	 * Get the reason a <code>SblockUser</code> was banned.
	 * 
	 * @param name
	 *            the name of the banned <code>SblockUser</code>
	 * @param ip
	 *            the IP of the banned <code>SblockUser</code>
	 * @return the ban reason
	 */
	public String getBanReason(String name, String ip) {
		return BannedPlayers.getBanReason(name, ip);
	}

	/**
	 * Add a ban and reason to a <code>SblockUser</code>.
	 * 
	 * @param target
	 *            the <code>SblockUser</code> to add a ban for
	 * @param reason
	 *            the reason the <code>SblockUser</code> was banned
	 */
	public void addBan(SblockUser target, String reason) {
		BannedPlayers.addBan(target, reason);
	}

	/**
	 * Remove a ban by name or IP.
	 * 
	 * @param target
	 *            the name or IP to unban
	 */
	public void removeBan(String target) {
		BannedPlayers.loadAndDeleteBans(target);
	}
}
