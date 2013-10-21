package co.sblock.Sblock.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import co.sblock.Sblock.DatabaseManager;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author FireNG, Jikoo
 */
public class UserManager {

	/** The <code>UserManager</code> instance. */
	private static UserManager manager;

	/**
	 * The <code>Map</code> of <code>Player</code> names and relevant
	 * <code>SblockUsers</code> currently online.
	 */
	private Map<String, SblockUser> users;

	/**
	 * Constructor for UserManager.
	 */
	UserManager() {
		manager = this;
		this.users = new HashMap<String, SblockUser>();
	}

	/**
	 * Adds a <code>Player</code> that has logged on to the users list
	 * 
	 * @param player
	 *            The <code>Player</code> that has logged on
	 */
	public void addUser(Player player) {
		users.put(player.getName(), new SblockUser(player.getName()));
	}

	/**
	 * Removes a <code>Player</code> from the users list.
	 * 
	 * @param player
	 *            The <code>Player</code> to remove
	 */
	public void removeUser(Player player) {
		SblockUser user = this.getUser(player.getName());
		if (user != null) {
			player.closeInventory();
			user.stopPendingTasks();
			DatabaseManager.getDatabaseManager().saveUserData(user);
		}
		users.remove(player.getName());
	}

	/**
	 * Removes a <code>Player</code> from the users list.
	 * 
	 * @param player
	 *            The <code>Player</code> to remove
	 */
	public void removeUser(String player) {
		SblockUser user = this.getUser(player);
		if (user != null) {
			user.getPlayer().closeInventory();
			user.stopPendingTasks();
			DatabaseManager.getDatabaseManager().saveUserData(user);
		}
		users.remove(player);
	}

	/**
	 * Gets a <code>SblockUser</code>by <code>Player</code> name.
	 * 
	 * @param name
	 *            The name of the <code>Player</code> to look up
	 * 
	 * @return The <code>SblockUser</code> associated with the given
	 *         <code>Player</code>, or <code>null</code> if no
	 *         <code>Player</code> with the given name is currently online.
	 */
	public SblockUser getUser(String name) {
		return users.get(name);
	}

	/**
	 * Gets a <code>Collection</code> of <code>SblockUsers</code> currently online.
	 * 
	 * @return the <code>SblockUsers</code> currently online
	 */
	public Collection<SblockUser> getUserlist() {
		return this.users.values();
	}

	/**
	 * Gets the <code>UserManager</code> instance.
	 * 
	 * @return the <code>UserManager</code> instance
	 */
	public static UserManager getUserManager() {
		if (manager == null)
			manager = new UserManager();
		return manager;
	}

}
