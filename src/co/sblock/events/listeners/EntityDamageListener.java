package co.sblock.events.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

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
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		switch (event.getCause()) {
		case BLOCK_EXPLOSION:
		case ENTITY_ATTACK:
		case ENTITY_EXPLOSION:
			if (event.getEntity().getType() == EntityType.DROPPED_ITEM) {
				event.setCancelled(true);
				return;
			}
		default:
			break;
		}
	}
}
