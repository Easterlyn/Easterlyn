/**
 * 
 */
package co.sblock.Sblock.Machines;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Module;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * @author Jikoo
 *
 */
public class MachineModule extends Module {

	private static MachineModule instance;

	private MachineManager manager;

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		Sblogger.info("SburbMachines", "Enabling Machines");
		instance = this;
		manager = new MachineManager();
		this.registerCommands(new MachineCommand());
		this.registerEvents(new MachineEvents());
		DatabaseManager.getDatabaseManager().loadAllMachines();
		Sblogger.info("SburbMachines", "Machines enabled");
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		manager.saveToDb();
		instance = null;
		manager = null;
	}

	public MachineManager getManager() {
		return this.manager;
	}

	public static MachineModule getInstance() {
		return instance;
	}
}
