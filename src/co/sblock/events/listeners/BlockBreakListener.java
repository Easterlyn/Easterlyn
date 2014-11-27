package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.progression.ServerMode;

/**
 * Listener for BlockBreakEvents.
 * 
 * @author Jikoo
 */
public class BlockBreakListener implements Listener {

	/**
	 * The event handler for Machine deconstruction.
	 * 
	 * @param event the BlockBreakEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Machine m = SblockMachines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleBreak(event));
		}

		User u = UserManager.getUser(event.getPlayer().getUniqueId());
		if (u != null && u.isServer()) {
			event.setCancelled(!ServerMode.getInstance().isWithinRange(u, event.getBlock()));
		}
	}
}
