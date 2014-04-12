package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import co.sblock.Sblock.UserData.User;
import co.sblock.Sblock.Utilities.Spectator.Spectators;

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
		if (Spectators.getSpectators().isSpectator(event.getEntity().getUniqueId())) {
			event.setCancelled(true);
		}

		User user = User.getUser(event.getEntity().getUniqueId());
		if (user != null && user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
