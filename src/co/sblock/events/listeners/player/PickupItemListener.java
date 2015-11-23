package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for PlayerPickupItemEvents.
 * 
 * @author Jikoo
 */
public class PickupItemListener extends SblockListener {

	public PickupItemListener(Sblock plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for PlayerPickupItemEvents.
	 * 
	 * @param event the PlayerPickupItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		OfflineUser offUser = Users.getGuaranteedUser(getPlugin(), event.getPlayer().getUniqueId());
		if (!(offUser instanceof OnlineUser)) {
			return;
		}
		OnlineUser user = (OnlineUser) offUser;

		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
