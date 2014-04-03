package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Server.ServerMode;

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

		if (SblockUser.getUser(event.getPlayer().getName()).isServer()) {
			// This should override any opening inventories.
			event.getPlayer().openInventory(ServerMode.getInstance().getInventory());
		}
	}
}
