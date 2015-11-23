package co.sblock.events.listeners.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for FoodLevelChangeEvents.
 * 
 * @author Jikoo
 */
public class FoodLevelChangeListener extends SblockListener {

	public FoodLevelChangeListener(Sblock plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for FoodLevelChangeEvents.
	 * 
	 * @param event the FoodLevelChangeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) {
			return;
		}

		OfflineUser user = Users.getGuaranteedUser(getPlugin(), event.getEntity().getUniqueId());
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
