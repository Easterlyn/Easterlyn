package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import com.easterlyn.utilities.tuple.Pair;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listener for InventoryPickupItemEvents.
 *
 * @author Jikoo
 */
public class InventoryPickupItemListener extends EasterlynListener {

	private final Machines machines;

	public InventoryPickupItemListener(Easterlyn plugin) {
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
		if (ih instanceof BlockState) {
			Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(((BlockState) ih).getBlock());
			if (pair != null) {
				event.setCancelled(pair.getLeft().handleHopperPickupItem(event, pair.getRight()));
			}
		}
	}

}
