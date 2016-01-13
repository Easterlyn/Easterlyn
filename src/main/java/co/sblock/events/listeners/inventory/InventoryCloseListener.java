package co.sblock.events.listeners.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.MachineInventoryTracker;
import co.sblock.machines.Machines;

/**
 * Listener for InventoryCloseEvents.
 * 
 * @author Jikoo
 */
public class InventoryCloseListener extends SblockListener {

	private final MachineInventoryTracker tracker;

	public InventoryCloseListener(Sblock plugin) {
		super(plugin);
		this.tracker = plugin.getModule(Machines.class).getInventoryTracker();
	}

	/**
	 * EventHandler for InventoryCloseEvents.
	 * 
	 * @param event the InventoryCloseEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		tracker.closeMachine(event);
	}
}
