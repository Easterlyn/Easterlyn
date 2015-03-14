package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

/**
 * Listener for BlockDispenseEvents.
 * 
 * @author Jikoo
 */
public class BlockDispenseListener implements Listener {

	/**
	 * EventHandler for BlockDispenseEvents.
	 * 
	 * @param event the BlockDispenseEvent.
	 */
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		// No negative stacks for infinite dispensers (unless we use this as a feature later)
		if (event.getItem().getAmount() < 1) {
			event.setCancelled(true);
			event.setItem(null);
		}
	}
}
