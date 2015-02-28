package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import co.sblock.machines.type.Computer;
import co.sblock.users.Users;

/**
 * Listener for InventoryOpenEvents.
 * 
 * @author Jikoo
 */
public class InventoryOpenListener implements Listener {

	/**
	 * EventHandler for InventoryOpenEvents.
	 * 
	 * @param event the InventoryOpenEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (Users.getGuaranteedUser(event.getPlayer().getUniqueId()).isServer()
				&& event.getInventory().getHolder() != null
				&& !(event.getInventory().getHolder() instanceof Computer)) {
			event.setCancelled(true);
		}
	}
}
