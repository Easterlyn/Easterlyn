package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for FoodLevelChangeEvents.
 * 
 * @author Jikoo
 */
public class FoodLevelChangeListener implements Listener {

	/**
	 * EventHandler for FoodLevelChangeEvents.
	 * 
	 * @param event the FoodLevelChangeEvent
	 */
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (Spectators.getInstance().isSpectator(event.getEntity().getUniqueId())) {
			event.setCancelled(true);
		}

		OfflineUser user = Users.getGuaranteedUser(event.getEntity().getUniqueId());
		if (user != null && user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
