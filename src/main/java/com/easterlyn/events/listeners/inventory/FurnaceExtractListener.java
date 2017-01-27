package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.Effects;
import com.easterlyn.events.listeners.SblockListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceExtractEvent;

/**
 * Listener for FurnaceExtractEvents.
 * 
 * @author Jikoo
 */
public class FurnaceExtractListener extends SblockListener {

	private final Effects effects;

	public FurnaceExtractListener(Easterlyn plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFurnaceExtract(FurnaceExtractEvent event) {
		effects.handleEvent(event, event.getPlayer(), true);
	}

}
