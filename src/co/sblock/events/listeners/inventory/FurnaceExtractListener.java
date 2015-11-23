package co.sblock.events.listeners.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceExtractEvent;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for FurnaceExtractEvents.
 * 
 * @author Jikoo
 */
public class FurnaceExtractListener extends SblockListener {

	private final Effects effects;

	public FurnaceExtractListener(Sblock plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFurnaceExtract(FurnaceExtractEvent event) {
		effects.handleEvent(event, event.getPlayer(), false);
	}

}
