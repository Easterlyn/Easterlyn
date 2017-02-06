package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.MachineInventoryTracker;
import com.easterlyn.machines.Machines;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Listener for InventoryCloseEvents.
 * 
 * @author Jikoo
 */
public class InventoryCloseListener extends EasterlynListener {

	private final MachineInventoryTracker tracker;

	public InventoryCloseListener(Easterlyn plugin) {
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
