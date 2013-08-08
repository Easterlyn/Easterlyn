package co.sblock.Sblock.UserData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * This module holds player information and provides methods for other modules
 * to access that data, as well as interfacing with the database.
 * 
 * @author FireNG, Dublek
 * 
 */
public class UserDataModule extends Module {

	/*
	 * (non-Javadoc)
	 * 
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		Sblogger.info("SblockUserData", "Enabling UserData Module");
		// Initialize the player manager
		UserManager.getUserManager();
		this.registerCommands(new UserDataCommands());
		this.registerEvents(new UserDataEvents());

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			UserManager.getUserManager().addUser(p);
		}

		Sblogger.info("SblockUserData", "UserData Module enabled");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		// TODO Auto-generated method stub

	}

}
