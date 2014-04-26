package co.sblock.events.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;

import co.sblock.utilities.meteors.MeteorMod;

/**
 * Listener for EntityDamageByBlockEvents.
 * 
 * @author Jikoo
 */
public class EntityDamageByBlockListener implements Listener {

	/**
	 * EventHandler for EntityDamageByBlockEvents.
	 * 
	 * @param event the EntityDamageByBlockEvent
	 */
	@EventHandler
	public void onEntityDamage(EntityDamageByBlockEvent event) {
		// Null damager means plugin-created. Should be a meteor.
		if (event.getDamager() != null) {
			return;
		}
		if (event.getEntityType() == EntityType.DROPPED_ITEM || event.getEntityType() == EntityType.PLAYER
				|| (event.getEntityType() == EntityType.FALLING_BLOCK && MeteorMod.getInstance().getBore())) {
			event.setCancelled(true);
		}
	}
}
