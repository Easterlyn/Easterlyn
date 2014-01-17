package co.sblock.Sblock.Machines;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Database.SblockData;

/**
 * @author Jikoo
 */
public class SblockMachines extends Module {

	/** The <code>MachineModule</code> instance. */
	private static SblockMachines instance;

	/** The <code>MachineManager</code>. */
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
		this.registerEvents(new MachineEvents());
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
	public static SblockMachines getMachines() {
		return instance;
	}
}
