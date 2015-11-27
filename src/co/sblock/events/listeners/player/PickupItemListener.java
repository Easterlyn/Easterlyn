package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Listener for PlayerPickupItemEvents.
 * 
 * @author Jikoo
 */
public class PickupItemListener extends SblockListener {

	private final Users users;

	public PickupItemListener(Sblock plugin) {
		super(plugin);
		this.users = plugin.getModule(Users.class);
	}

	/**
	 * EventHandler for PlayerPickupItemEvents.
	 * 
	 * @param event the PlayerPickupItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		User user = users.getUser(event.getPlayer().getUniqueId());
		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
