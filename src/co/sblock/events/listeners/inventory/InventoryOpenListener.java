package co.sblock.events.listeners.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.type.Computer;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for InventoryOpenEvents.
 * 
 * @author Jikoo
 */
public class InventoryOpenListener extends SblockListener {

	public InventoryOpenListener(Sblock plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for InventoryOpenEvents.
	 * 
	 * @param event the InventoryOpenEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		OfflineUser user = Users.getGuaranteedUser(getPlugin(), event.getPlayer().getUniqueId());
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()
				&& event.getInventory().getHolder() != null
				&& !(event.getInventory().getHolder() instanceof Computer)) {
			event.setCancelled(true);
		}
	}
}
