/**
 * 
 */
package co.sblock.Sblock.Machines;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;

/**
 * @author Jikoo
 *
 */
public class MachineModule extends Module {

	private static MachineModule instance;

	private InventoryEventHandler invHandler;
	private MachineManager manager;

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		invHandler = new InventoryEventHandler();
		manager = new MachineManager();
		Bukkit.getPluginManager().registerEvents(invHandler, Sblock.getInstance());
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		HandlerList.unregisterAll(invHandler);
		instance = null;
		invHandler = null;
		manager = null;
	}

	public InventoryEventHandler getInvHandler() {
		return this.invHandler;
	}

	public MachineManager getManager() {
		return this.manager;
	}

	public static MachineModule getInstance() {
		return instance;
	}
}
