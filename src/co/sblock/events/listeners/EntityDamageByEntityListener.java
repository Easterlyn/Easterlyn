package co.sblock.events.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
