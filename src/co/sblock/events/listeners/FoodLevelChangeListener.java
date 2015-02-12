package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

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
		OfflineUser user = Users.getGuaranteedUser(event.getEntity().getUniqueId());
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
