package co.sblock.Sblock.UserData;

import co.sblock.Sblock.Module;

/**
 * This module holds player information and provides methods for other modules
 * to access that data, as well as interfacing with the database.
 * 
 * @author FireNG
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
		// Initialize the player manager
		UserManager.getUserManager();
		this.registerCommands(new UserDataCommands());
		this.registerEvents(new UserDataEvents());

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
