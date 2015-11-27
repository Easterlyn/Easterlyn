package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Listener for PlayerInteractEntityEvents.
 * 
 * @author Jikoo
 */
public class InteractEntityListener extends SblockListener {

	private final Users users;

	public InteractEntityListener(Sblock plugin) {
		super(plugin);
		this.users = plugin.getModule(Users.class);
	}

	/**
	 * EventHandler for PlayerInteractEntityEvents.
	 * 
	 * @param event the PlayerInteractEntityEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		User user = users.getUser(event.getPlayer().getUniqueId());
		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
