package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * Listener for PlayerInteractEntityEvents.
 * 
 * @author Jikoo
 */
public class PlayerInteractEntityListener implements Listener {

	/**
	 * EventHandler for PlayerInteractEntityEvents.
	 * 
	 * @param event the PlayerInteractEntityEvent
	 */
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user != null && user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
