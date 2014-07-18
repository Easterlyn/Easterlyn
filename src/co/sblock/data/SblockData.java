package co.sblock.data;

import java.sql.Connection;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.tmathmeyer.jadis.async.Promise;

import co.sblock.chat.channel.Channel;
import co.sblock.data.redis.RedisClient;
import co.sblock.data.sql.SQLClient;
import co.sblock.machines.type.Machine;
import co.sblock.utilities.Log;

/**
 * Collection of all database-related functions.
 * 
 * @author Jikoo, FireNG, tmathmeyer
 */
public abstract class SblockData {
	
	/**
	 * 
	 * @author ted
	 * 
	 * a wrapper for toggling between SQL/Redis
	 *
	 */
	private static enum Implementation {
		SQL(new SQLClient()),
		REDIS(new RedisClient());
		public final SblockData data;
		private Implementation(SblockData data) {
			this.data = data;
		}
		public Implementation other() {
			if (this == SQL) {
				return REDIS;
			}
			return SQL;
		}
	}

	private static Implementation impl = Implementation.SQL;

	/**
	 * SblockData singleton.
	 * 
	 * @return the SblockData instance
	 */
	public static SblockData getDB() {
		return impl.data;
	}

	/**
	 * switch the implementation
	 */
	public static String toggleDBImpl() {
		impl = impl.other();
		return impl.name();
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
	public abstract void loadUserData(UUID userID);

	/**
	 * TODO: describe this with a real javadoc comment
	 */
	public abstract void startOfflineLookup(CommandSender sender, String name);

	/**
	 * Delete specified Player's data from database.
	 * 
	 * @param name the name of the Player whose data is to be deleted
	 */
	public abstract void deleteUser(UUID userID);

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
	 * Get a User's name by the IP they last connected with.
	 * 
	 * @param hostAddress the IP to look up
	 * 
	 * @return the name of the User, "Player" if invalid
	 */
	public abstract void getUserFromIP(String hostAddress, Promise<String> executor);

	/**
	 * sets the database into a "finalize" mode, so that nothing can be pulled out,
	 * or in the case of redis, the calls become synchronous
	 */
	public abstract void enterFinalizeMode();
}
