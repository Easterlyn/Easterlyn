package co.sblock.Sblock.Events.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Listener for all InventoryDragEvents.
 * 
 * @author Jikoo
 */
public class InventoryDragListener implements Listener {

	/**
	 * EventHandler for all InventoryDragEvents.
	 * 
	 * @param event the InventoryDragEvent
	 */
	@EventHandler
	public void onItemDragDrop(InventoryDragEvent event) {
		if (event.getInventory() == null || !(event.getWhoClicked() instanceof Player)) {
			return;
		}
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			for (int i : event.getRawSlots()) {
				if (event.getView().getTopInventory().getSize() > i) {
					event.setResult(Result.DENY);
					break;
				}
			}
		}
	}
}
