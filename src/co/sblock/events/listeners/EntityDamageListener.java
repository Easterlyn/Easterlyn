package co.sblock.events.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * Listener for EntityDamageEvents.
 * 
 * @author Jikoo
 */
public class EntityDamageListener implements Listener {

	/**
	 * EventHandler for EntityDamageEvents.
	 * 
	 * @param event the EntityDamageEvent
	 */
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.DROPPED_ITEM
				&& (event.getCause() == DamageCause.ENTITY_EXPLOSION
				|| event.getCause() == DamageCause.BLOCK_EXPLOSION
				|| event.getCause() == DamageCause.CUSTOM)) {
			// I sincerely doubt that this will affect anyone seriously.
			// There are far nicer trash can designs.
			event.setCancelled(true);
		}
		// TODO meteors no damage to players
	}
}
