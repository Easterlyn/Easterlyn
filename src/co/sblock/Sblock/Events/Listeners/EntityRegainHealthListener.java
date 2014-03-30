package co.sblock.Sblock.Events.Listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Spectator.Spectators;

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

		if (Spectators.getSpectators().isSpectator(p.getName())) {
			event.setCancelled(true);
			return;
		}

		SblockUser u = SblockUser.getUser(p.getName());
		if (u != null && u.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
