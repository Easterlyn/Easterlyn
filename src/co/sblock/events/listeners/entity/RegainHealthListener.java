package co.sblock.events.listeners.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for EntityRegainHealthEvents.
 * 
 * @author Jikoo
 */
public class RegainHealthListener extends SblockListener {

	public RegainHealthListener(Sblock plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for EntityRegainHealthEvents.
	 * 
	 * @param event the EntityRegainHealthEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
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
