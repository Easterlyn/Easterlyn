package co.sblock.events.listeners.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;

import co.sblock.effects.Effects;

/**
 * Listener for FurnaceExtractEvents.
 * 
 * @author Jikoo
 */
public class FurnaceExtractListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFurnaceExtract(FurnaceExtractEvent event) {
		Effects.getInstance().handleEvent(event, event.getPlayer(), false);
	}

}
