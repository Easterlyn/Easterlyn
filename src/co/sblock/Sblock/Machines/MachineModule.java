package co.sblock.Sblock.Machines;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Module;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * @author Jikoo
 */
public class MachineModule extends Module {

	/** The <code>MachineModule</code> instance. */
	private static MachineModule instance;

	/** The <code>MachineManager</code>. */
	private MachineManager manager;

	/**
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
	 * Gets the <code>MachineManager</code>.
	 * 
	 * @return the <code>MachineManager</code>
	 */
	public MachineManager getManager() {
		return this.manager;
	}

	/**
	 * Gets the current instance of MachineModule.
	 * 
	 * @return the <code>MachineModule</code>
	 */
	public static MachineModule getInstance() {
		return instance;
	}
}
