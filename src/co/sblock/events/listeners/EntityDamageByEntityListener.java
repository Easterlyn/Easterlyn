package co.sblock.events.listeners;

import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import co.sblock.Sblock;
import co.sblock.events.Events;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;
import co.sblock.utilities.meteors.MeteoriteComponent;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;

/**
 * Listener for EntityDamageByEntityEvents.
 * 
 * @author Jikoo
 */
public class EntityDamageByEntityListener implements Listener {

	/**
	 * EventHandler for EntityDamageByEntityEvents.
	 * 
	 * @param event the EntityDamageByEntityEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntityType() == EntityType.DROPPED_ITEM) {
			event.setCancelled(true);
			return;
		}
		if (event.getDamager().getType() == EntityType.FALLING_BLOCK
				&& ((CraftEntity) event.getDamager()).getHandle() instanceof MeteoriteComponent) {
			if (event.getEntityType() == EntityType.PLAYER) {
				event.setCancelled(true);
				return;
			}
			if (event.getEntityType() == EntityType.FALLING_BLOCK
					&& ((MeteoriteComponent) ((CraftEntity)event.getDamager()).getHandle()).shouldBore()) {
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

		final UUID damaged = event.getEntity().getUniqueId();
		BukkitTask oldTask = Events.getInstance().getPVPTasks().put(damaged, new BukkitRunnable() {
			@Override
			public void run() {
				if (Events.getInstance().getPVPTasks().containsKey(damaged)) {
					Events.getInstance().getPVPTasks().remove(damaged);
				}
			}
		}.runTaskLater(Sblock.getInstance(), 100L));
		if (oldTask != null) {
			oldTask.cancel();
		}

		OfflineUser damagerUser = Users.getGuaranteedUser(damager);
		OfflineUser damagedUser = Users.getGuaranteedUser(damaged);
		if (damagerUser instanceof OnlineUser && ((OnlineUser) damagerUser).isServer()
				|| damagedUser instanceof OnlineUser && ((OnlineUser) damagedUser).isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
