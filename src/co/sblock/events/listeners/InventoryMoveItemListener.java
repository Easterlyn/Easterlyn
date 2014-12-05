package co.sblock.events.listeners;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for InventoryMoveItemEvents.
 * 
 * @author Jikoo
 */
public class InventoryMoveItemListener implements Listener {

	/**
	 * EventHandler for when hoppers move items.
	 * 
	 * @param event the InventoryMoveItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		InventoryHolder ih = event.getDestination().getHolder();
		if (ih != null && ih instanceof BlockState) {
			Machine m = Machines.getInstance().getMachineByBlock(((BlockState) ih).getBlock());
			if (m != null) {
				event.setCancelled(m.handleHopperMoveItem(event));
				return;
			}
		}
	}
}
