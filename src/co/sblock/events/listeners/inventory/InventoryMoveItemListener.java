package co.sblock.events.listeners.inventory;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
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
		// TODO: check sending inv as well
		if (ih != null && ih instanceof BlockState) {
			Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(((BlockState) ih).getBlock());
			if (pair != null) {
				event.setCancelled(pair.getLeft().handleHopperMoveItem(event, pair.getRight()));
			}
		}
	}
}
