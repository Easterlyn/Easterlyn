package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import co.sblock.users.OfflineUser;
import co.sblock.users.UserManager;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for PlayerPickupItemEvents.
 * 
 * @author Jikoo
 */
public class PlayerPickupItemListener implements Listener {

	/**
	 * EventHandler for PlayerPickupItemEvents.
	 * 
	 * @param event the PlayerPickupItemEvent
	 */
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			return;
		}

		// valid SblockUser required for all events below this point
		OfflineUser user = UserManager.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user == null) {
			return;
		}

		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
