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
	@EventHandler
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

		if (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile
				&& ((Projectile) event.getDamager()).getShooter() instanceof Player) {
			// Player or player-shot projectile
			final UUID uuid = event.getEntity().getUniqueId();
			BukkitTask oldTask = Events.getInstance().getPVPTasks().put(uuid, new BukkitRunnable() {
				@Override
				public void run() {
					if (Events.getInstance().getPVPTasks().containsKey(uuid)) {
						Events.getInstance().getPVPTasks().remove(uuid);
					}
				}
			}.runTaskLater(Sblock.getInstance(), 100L));
			if (oldTask != null) {
				oldTask.cancel();
			}
		}

		if (!(event.getDamager() instanceof Player)) {
			return;
		}

		OfflineUser user = Users.getGuaranteedUser(event.getDamager().getUniqueId());
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
