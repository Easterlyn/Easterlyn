package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.meteors.MeteoriteComponent;
import co.sblock.utilities.spectator.Spectators;

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

		Player p = (Player) event.getDamager();

		if (Spectators.getInstance().isSpectator(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED + "You waggle your fingers wildly, but your target remains unmussed.");
			event.setCancelled(true);
			return;
		}

		OfflineUser u = Users.getGuaranteedUser(p.getUniqueId());
		if (u != null && u.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
