package com.easterlyn.events.listeners.entity;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.micromodules.Meteors;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * Listener for EntityChangeBlockEvents.
 * 
 * @author Jikoo
 */
public class ChangeBlockListener extends SblockListener {

	private final Meteors meteors;

	public ChangeBlockListener(Easterlyn plugin) {
		super(plugin);
		this.meteors = plugin.getModule(Meteors.class);
	}

	/**
	 * EventHandler for EntityChangeBlockEvents to handle Meteorite FallingBlock landings.
	 * 
	 * @param event the EntityChangeBlockEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.getEntityType() != EntityType.FALLING_BLOCK) {
			return;
		}
		meteors.handlePotentialMeteorite(event);
	}

}
