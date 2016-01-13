package co.sblock.events.listeners.entity;

import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for EntityDeathEvents.
 * 
 * @author Jikoo
 */
public class DeathListener extends SblockListener {

	private final Effects effects;

	public DeathListener(Sblock plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
	}

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
			effects.handleEvent(event, (LivingEntity) entityDamageEvent.getDamager(), false);
		}
	}

}
