package co.sblock.events.listeners.inventory;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for InventoryPickupItemEvents.
 * 
 * @author Jikoo
 */
public class InventoryPickupItemListener extends SblockListener {

	private final Machines machines;

	public InventoryPickupItemListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for when hoppers pick up items.
	 * 
	 * @param event the InventoryPickupItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryPickupItemEvent event) {
		InventoryHolder ih = event.getInventory().getHolder();
		if (ih != null && ih instanceof BlockState) {
			Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(((BlockState) ih).getBlock());
			if (pair != null) {
				event.setCancelled(pair.getLeft().handleHopperPickupItem(event, pair.getRight()));
			}
		}
	}
}
