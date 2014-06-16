package co.sblock.machines;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

import co.sblock.data.SblockData;
import co.sblock.machines.type.Machine;
import co.sblock.module.Module;

/**
 * @author Jikoo
 */
public class SblockMachines extends Module {

	/** The MachineModule instance. */
	private static SblockMachines instance;

	/** The MachineManager. */
	private MachineManager manager;

	/**
	 * @see co.sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		manager = new MachineManager();
		this.registerCommands(new MachineCommand());
		SblockData.getDB().loadAllMachines();

		// Recipe does nothing on its own - We'll cancel all smelts that do not happen within a TotemLate.
		Bukkit.addRecipe(new FurnaceRecipe(new ItemStack(Material.NETHER_BRICK_ITEM), Material.NETHER_BRICK_ITEM));
	}

	/**
	 * @see co.sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		for (Machine m : manager.getMachines()) {
			m.disable();
			if (m.getData() != null) {
				SblockData.getDB().saveMachine(m);
			}
		}
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
