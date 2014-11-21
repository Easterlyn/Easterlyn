package co.sblock.events.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import co.sblock.utilities.meteors.MeteorMod;

/**
 * Listener for EntityChangeBlockEvents.
 * 
 * @author Jikoo
 */
public class EntityChangeBlockListener implements Listener {

	/**
	 * EventHandler for EntityChangeBlockEvents to handle Meteorite FallingBlock landings.
	 * 
	 * @param event the EntityChangeBlockEvent
	 */
	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.getEntityType() != EntityType.FALLING_BLOCK) {
			return;
		}
		MeteorMod.getInstance().handlePotentialMeteorite(event);
	}
}
