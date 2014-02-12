package co.sblock.Sblock.Machines;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Database.SblockData;

/**
 * @author Jikoo
 */
public class SblockMachines extends Module {

	/** The MachineModule instance. */
	private static SblockMachines instance;

	/** The MachineManager. */
	private MachineManager manager;

	/**
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		getLogger().fine("Enabling Machines");
		instance = this;
		manager = new MachineManager();
		this.registerCommands(new MachineCommand());
		SblockData.getDB().loadAllMachines();
		getLogger().fine("Machines enabled");
	}

	/**
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		manager.saveToDb();
		instance = null;
		manager = null;
	}

	/**
	 * Gets the MachineManager.
	 * 
	 * @return the MachineManager
	 */
	public MachineManager getManager() {
		return this.manager;
	}

	/**
	 * Gets the current instance of MachineModule.
	 * 
	 * @return the MachineModule
	 */
	public static SblockMachines getMachines() {
		return instance;
	}
}
