package co.sblock.events.listeners.entity;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.events.Events;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.MeteoriteComponent;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;

/**
 * Listener for EntityDamageByEntityEvents.
 * 
 * @author Jikoo
 */
public class DamageByEntityListener extends SblockListener {

	private final Effects effects;
	private final Events events;

	public DamageByEntityListener(Sblock plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
		this.events = plugin.getModule(Events.class);
	}

	/**
	 * EventHandler for EntityDamageByEntityEvents.
	 * 
	 * @param event the EntityDamageByEntityEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntityType() == EntityType.DROPPED_ITEM
				&& event.getDamager().getType() != EntityType.WITHER
				&& event.getDamager().getType() != EntityType.WITHER_SKULL) {
			event.setCancelled(true);
			return;
		}
		if (event.getDamager().getType() == EntityType.FALLING_BLOCK
				&& ((CraftEntity) event.getDamager()).getHandle() instanceof MeteoriteComponent) {
			if (event.getEntityType() == EntityType.PLAYER) {
				event.setCancelled(true);
				return;
			}
		}

		final UUID damager;
		if (event.getDamager() instanceof Player) {
			damager = event.getDamager().getUniqueId();
		} else if (event.getDamager() instanceof Projectile
				&& ((Projectile) event.getDamager()).getShooter() instanceof Player) {
			damager = ((Player) ((Projectile) event.getDamager()).getShooter()).getUniqueId();
		} else {
			return;
		}

		Player damagerPlayer = Bukkit.getPlayer(damager);
		if (damagerPlayer != null) {
			effects.handleEvent(event, damagerPlayer, false);
		}

		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		final UUID damaged = event.getEntity().getUniqueId();
		if (damaged.equals(damager)) {
			// Deserved.
			return;
		}

		BukkitTask oldTask = events.getPVPTasks().put(damaged, new BukkitRunnable() {
			@Override
			public void run() {
				if (events.getPVPTasks().containsKey(damaged)) {
					events.getPVPTasks().remove(damaged);
				}
			}
		}.runTaskLater(getPlugin(), 100L));
		if (oldTask != null) {
			oldTask.cancel();
		}
	}
}
