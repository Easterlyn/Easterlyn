package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import co.sblock.events.SblockEvents;
import co.sblock.machines.type.Computer;
import co.sblock.users.User;
import co.sblock.utilities.progression.ServerMode;

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

		if (User.getUser(event.getPlayer().getUniqueId()).isServer()
				&& event.getInventory().getHolder() != null
				&& !(event.getInventory().getHolder() instanceof ServerMode)
				&& !(event.getInventory().getHolder() instanceof Computer)) {
			event.setCancelled(true);
			event.getPlayer().openInventory(ServerMode.getInstance().getInventory());
		}
	}
}
