package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import co.sblock.Sblock.Machines.Type.Machine;

/**
 * Listener for InventoryClickEvents.
 * 
 * @author Jikoo
 */
public class InventoryClickListener implements Listener {

	/**
	 * EventHandler for all InventoryClickEvents.
	 * 
	 * @param event the InventoryClickEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		//Adam check for totem lathe furnace
		InventoryHolder ih = event.getView().getTopInventory().getHolder();
		if (ih == null || !(ih instanceof Machine)) {
			return;
		}
		Machine m = (Machine) ih;
		if (m != null) {
			event.setCancelled(m.handleClick(event));
		}
	}
}
