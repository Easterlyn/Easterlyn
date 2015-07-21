package co.sblock.events.listeners.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import co.sblock.effects.Effects;

/**
 * Listener for EntityDamageEvents.
 * 
 * @author Jikoo
 */
public class DamageListener implements Listener {

	/**
	 * EventHandler for EntityDamageEvents.
	 * 
	 * @param event the EntityDamageEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Effects.getInstance().handleEvent(event, (Player) event.getEntity(), true);
	}
}
