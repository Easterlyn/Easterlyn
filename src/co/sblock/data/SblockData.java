package co.sblock.data;

import java.sql.Connection;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import co.sblock.chat.channel.Channel;
import co.sblock.data.sql.SQLClient;
import co.sblock.machines.type.Machine;
import co.sblock.users.TowerData;
import co.sblock.users.User;
import co.sblock.utilities.Log;

/**
 * Collection of all database-related functions.
 * 
 * @author Jikoo, FireNG, tmathmeyer
 */
public abstract class SblockData {
	
	/*
	 * The SblockData instance. avoid lazy loading... we should take the instantiation hit
	 * on startup, not at some arbitrary point afterwards
	 * also provides an easy way to switch between redis / mysql, though this should (and will, later)
	 * be controlled by a config file
	 */
	private static SblockData db = new SQLClient();
	//private static final SblockData db = new RedisClient();

	/**
	 * SblockData singleton.
	 * 
	 * @return the SblockData instance
	 */
	public static SblockData getDB() {
		return db;
	}

	/**
	 * returns the custom named logger object... may differ across implementations
	 * 
	 * @return the sblockdata logger
	 */
	public abstract Log getLogger();

	/**
	 * Establish connection to database and create SblockData instance.
	 * 
	 * @return true if enabled successfully
	 */
	public abstract boolean enable();

	/**
	 * Close Connection and set instance to null.
	 */
	public abstract void disable();

	/**
	 * Get the Connection to the database.
	 * 
	 * @return the Connection
	 */
	protected abstract Connection connection();

	/**
	 * Initiate user data saving for a Player by name.
	 * 
	 * @param name the name of the Player
	 */
	public abstract void saveUserData(UUID userID);

	/**
	 * Initiate loading of a Player's stored data.
	 * 
	 * @param name the name of the Payer to load data for
	 */
	public abstract User loadUserData(UUID userID);

	/**
	 * TODO: describe this with a real javadoc comment
	 */
	public abstract void startOfflineLookup(CommandSender sender, String name);

	/**
	 * Delete specified Player's data from database.
	 * 
	 * @param name the name of the Player whose data is to be deleted
	 */
	public abstract void deleteUser(String name);

	/**
	 * Save Channel data to database.
	 * 
	 * @param c the Channel to save data for
	 */
	public abstract void saveChannelData(Channel c);

	/**
	 * Creates and loads all Channels from saved data.
	 */
	public abstract void loadAllChannelData();

	/**
	 * Delete a Channel by name.
	 * 
	 * @param channelName the name of the Channel to delete
	 */
	public abstract void deleteChannel(String channelName);

	/**
	 * Save Machine data to database.
	 * 
	 * @param m the Machine to save data for
	 */
	public abstract void saveMachine(Machine m);

	/**
	 * Delete a specified Machine's data from database.
	 * 
	 * @param m the Machine to delete data of
	 */
	public abstract void deleteMachine(Machine m);

	/**
	 * Creates and loads all Machines from saved data.
	 */
	public abstract void loadAllMachines();

	/**
	 * Fills out TowerData from saved data.
	 */
	public abstract void loadTowerData();

	/**
	 * Save all TowerData.
	 * 
	 * @param towers the TowerData to save
	 */
	public abstract void saveTowerData(TowerData towers);

	/**
	 * Get a User's name by the IP they last connected with.
	 * 
	 * @param hostAddress the IP to look up
	 * 
	 * @return the name of the User, "Player" if invalid
	 */
	public abstract String getUserFromIP(String hostAddress);

	/**
	 * Get the reason a User was banned.
	 * 
	 * @param user the name of UUID of the banned User
	 * @param ip the IP of the banned User
	 * 
	 * @return the ban reason
	 */
	public abstract String getBanReason(String user, String ip);

	/**
	 * Add a ban and reason to a User.
	 * 
	 * @param target the User to add a ban for
	 * @param reason the reason the User was banned
	 */
	public abstract void addBan(User target, String reason);

	/**
	 * Remove a ban by name, IP, or UUID.
	 * 
	 * @param target the name, IP, or UUID to unban
	 */
	public abstract void removeBan(String target);
}
