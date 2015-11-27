package co.sblock.events.listeners.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.type.Computer;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Listener for InventoryOpenEvents.
 * 
 * @author Jikoo
 */
public class InventoryOpenListener extends SblockListener {

	private final Users users;

	public InventoryOpenListener(Sblock plugin) {
		super(plugin);
		this.users = plugin.getModule(Users.class);
	}

	/**
	 * EventHandler for InventoryOpenEvents.
	 * 
	 * @param event the InventoryOpenEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		User user = users.getUser(event.getPlayer().getUniqueId());
		if (user.isServer() && event.getInventory().getHolder() != null
				&& !(event.getInventory().getHolder() instanceof Computer)) {
			event.setCancelled(true);
		}
	}
}
