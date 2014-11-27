package co.sblock.users;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.module.Module;

/**
 * This module holds player information and provides methods for other modules
 * to access that data, as well as interfacing with the database.
 * 
 * @author FireNG, Dublek, Jikoo
 */
public class SblockUsers extends Module {

	/** Map containing all server/client player requests */
	private Map<String, String> requests;

	private static SblockUsers instance;

	/**
	 * @see co.sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		requests = new HashMap<String, String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			UserManager.loadUser(p.getUniqueId());
		}
	}

	/**
	 * @see co.sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		for (User u : UserManager.getUsers().toArray(new User[0])) {
			UserManager.unloadUser(u.getUUID());
		}
		instance = null;
	}

	@Override
	protected String getModuleName() {
		return "SblockUserCore";
	}

	/**
	 * Gets a Map of all pending requests.
	 * 
	 * @return a Map of all pending requests
	 */
	public Map<String, String> getRequests() {
		return requests;
	}

	public static SblockUsers getSblockUsers() {
		return instance;
	}
}
