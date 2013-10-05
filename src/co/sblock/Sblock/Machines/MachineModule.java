/**
 * 
 */
package co.sblock.Sblock.Machines;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Module;

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
		instance = this;
		manager = new MachineManager();
		this.registerCommands(new MachineCommand());
		DatabaseManager.getDatabaseManager().loadAllMachines();
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
