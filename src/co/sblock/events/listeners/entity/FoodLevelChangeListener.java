package co.sblock.events.listeners.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Listener for FoodLevelChangeEvents.
 * 
 * @author Jikoo
 */
public class FoodLevelChangeListener extends SblockListener {

	private final Users users;

	public FoodLevelChangeListener(Sblock plugin) {
		super(plugin);
		this.users = plugin.getModule(Users.class);
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

		User user = users.getUser(event.getEntity().getUniqueId());
		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
