package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import com.easterlyn.utilities.tuple.Pair;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listener for InventoryMoveItemEvents.
 *
 * @author Jikoo
 */
public class InventoryMoveItemListener extends EasterlynListener {

	private final Machines machines;

	public InventoryMoveItemListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for when hoppers move items.
	 *
	 * @param event the InventoryMoveItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		InventoryHolder ih = event.getDestination().getHolder();
		// For now, sending inv is not checked, as no machines require it.
		if (ih instanceof BlockState) {
			Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(((BlockState) ih).getBlock());
			if (pair != null) {
				event.setCancelled(pair.getLeft().handleHopperMoveItem(event, pair.getRight()));
			}
		}
	}

}
