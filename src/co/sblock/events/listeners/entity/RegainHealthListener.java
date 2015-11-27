package co.sblock.events.listeners.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Listener for EntityRegainHealthEvents.
 * 
 * @author Jikoo
 */
public class RegainHealthListener extends SblockListener {

	private final Users users;

	public RegainHealthListener(Sblock plugin) {
		super(plugin);
		this.users = plugin.getModule(Users.class);
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

		User user = users.getUser(event.getEntity().getUniqueId());
		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
