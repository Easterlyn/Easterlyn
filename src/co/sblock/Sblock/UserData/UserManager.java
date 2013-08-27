package co.sblock.Sblock.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import co.sblock.Sblock.DatabaseManager;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author FireNG, Jikoo
 * 
 */
public class UserManager {

	private static UserManager manager;

	private Map<String, SblockUser> users;

	UserManager() {
		manager = this;
		this.users = new HashMap<String, SblockUser>();
	}

	/**
	 * Adds a player that has logged on to the users list
	 * 
	 * @param player
	 *            The player that has logged on
	 */
	public void addUser(Player player) {
		users.put(player.getName(), new SblockUser(player.getName()));
	}

	/**
	 * Removes a player from the users list that has left the server
	 * 
	 * @param player
	 *            The player to remove
	 */
	public void removeUser(Player player) {
		SblockUser user = this.getUser(player.getName());
		if (user != null) {
			player.closeInventory();
			user.stopPendingTasks();
			DatabaseManager.getDatabaseManager()
					.saveUserData(user);
		}
		users.remove(player.getName());
	}

	/**
	 * Removes a player from the users list that has left the server
	 * 
	 * @param player
	 *            The player to remove
	 */
	public void removeUser(String player) {
		SblockUser user = this.getUser(player);
		if (user != null) {
			user.getPlayer().closeInventory();
			user.stopPendingTasks();
			DatabaseManager.getDatabaseManager()
					.saveUserData(user);
		}
		users.remove(player);
	}

	/**
	 * @param name
	 *            The player to look up
	 * @return The SblockUser object associated with the given player, or null
	 *         if no player with the given name is currently online.
	 */
	public SblockUser getUser(String name) {

		return users.get(name);
	}

	public Collection<SblockUser> getUserlist() {
		return this.users.values();
	}

	/**
	 * Converts the given string into a Bukkit Location object
	 * 
	 * @param string
	 *            The string to convert
	 * @return The Location object represented by this string, or null if the
	 *         input string is null
	 * @throws IllegalArgumentException
	 *             if the given string is not valid
	 */
	public static Location parseLocation(String loc) {
		if (loc == null)
			return null;
		try {
			String[] parts = loc.split(",");
			return new Location(Bukkit.getWorld(parts[0]),
					Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
					Integer.parseInt(parts[3]));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"String is not in correct format.", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(
					"String is not in correct format.", e);
		}
	}

	/**
	 * Converts the given Bukkit Location object to a string for storing in the
	 * database.
	 * 
	 * @param loc
	 *            The Location object to convert
	 * @return a String representation of the object
	 */
	public static String locationToString(Location loc) {
		if (loc == null)
			return null;
		return loc.getWorld().getName() + "," + loc.getBlockX() + ","
				+ loc.getBlockY() + "," + loc.getBlockZ();
	}

	public static UserManager getUserManager() {
		if (manager == null)
			manager = new UserManager();
		return manager;
	}

}
