package co.sblock.Sblock.UserData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Database.SblockData;

/**
 * This module holds player information and provides methods for other modules
 * to access that data, as well as interfacing with the database.
 * 
 * @author FireNG, Dublek
 */
public class SblockUsers extends Module {

	/**
	 * Method onEnable.
	 * 
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		getLogger().fine("Enabling UserData Module");
		// Initialize the player manager
		UserManager.getUserManager();
		this.registerCommands(new UserDataCommands());

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			SblockData.getDB().loadUserData(p.getName());
		}

		getLogger().fine("[SblockUserData] UserData Module enabled");

	}

	/**
	 * Method onDisable.
	 * 
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
	}

}
