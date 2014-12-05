package co.sblock.events.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import co.sblock.users.OfflineUser;
import co.sblock.users.UserManager;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for EntityRegainHealthEvents.
 * 
 * @author Jikoo
 */
public class EntityRegainHealthListener implements Listener {

	/**
	 * EventHandler for EntityRegainHealthEvents.
	 * 
	 * @param event the EntityRegainHealthEvent
	 */
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) {
			return;
		}

		Player p = (Player) event.getEntity();

		if (Spectators.getSpectators().isSpectator(p.getUniqueId())) {
			event.setCancelled(true);
			return;
		}

		OfflineUser user = UserManager.getGuaranteedUser(p.getUniqueId());
		if (user != null && user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
