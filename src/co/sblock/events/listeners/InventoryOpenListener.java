package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import co.sblock.events.SblockEvents;
import co.sblock.machines.type.Computer;
import co.sblock.users.UserManager;

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
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {

		// Captchadex opening - events no longer need be cancelled.
		SblockEvents.getEvents().openingCaptchadex.remove(event.getPlayer().getName());

		if (UserManager.getUser(event.getPlayer().getUniqueId()).isServer()
				&& event.getInventory().getHolder() != null
				&& !(event.getInventory().getHolder() instanceof Computer)) {
			event.setCancelled(true);
		}
	}
}
