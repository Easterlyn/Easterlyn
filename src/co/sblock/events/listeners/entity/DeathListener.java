package co.sblock.events.listeners.entity;

import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import co.sblock.effects.Effects;

/**
 * Listener for EntityDeathEvents.
 * 
 * @author Jikoo
 */
public class DeathListener implements Listener {

	/**
	 * EventHandler for EntityDeathEvents.
	 * 
	 * @param event the EntityDeathEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			return;
		}
		if (event.getEntity().getKiller() != null
				&& event.getEntity().getKiller().getGameMode() == GameMode.CREATIVE) {
			event.setDroppedExp(0);
			event.getDrops().clear();
			return;
		}
		EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
		if (damageEvent instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) damageEvent;
			if (!(entityDamageEvent.getDamager() instanceof LivingEntity)) {
				return;
			}
			// Confusing point: Killer is the one with the active, not dying entity
			Effects.getInstance().handleEvent(event, (LivingEntity) entityDamageEvent.getDamager(), false);
		}
	}

}
