package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for PlayerInteractEntityEvents.
 * 
 * @author Jikoo
 */
public class InteractEntityListener extends SblockListener {

	public InteractEntityListener(Sblock plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for PlayerInteractEntityEvents.
	 * 
	 * @param event the PlayerInteractEntityEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		OfflineUser user = Users.getGuaranteedUser(getPlugin(), event.getPlayer().getUniqueId());
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
