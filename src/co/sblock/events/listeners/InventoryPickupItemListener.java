package co.sblock.events.listeners;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for InventoryPickupItemEvents.
 * 
 * @author Jikoo
 */
public class InventoryPickupItemListener implements Listener {

	/**
	 * EventHandler for when hoppers pick up items.
	 * 
	 * @param event the InventoryPickupItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryPickupItemEvent event) {
		InventoryHolder ih = event.getInventory().getHolder();
		if (ih != null && ih instanceof BlockState) {
			Machine m = Machines.getInstance().getMachineByBlock(((BlockState) ih).getBlock());
			if (m != null) {
				event.setCancelled(m.handleHopperPickupItem(event));
				return;
			}
		}
	}
}
