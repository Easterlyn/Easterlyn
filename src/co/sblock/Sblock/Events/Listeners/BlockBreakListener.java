package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.UserData.User;
import co.sblock.Sblock.Utilities.Server.ServerMode;

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
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleBreak(event));
		}

		User u = User.getUser(event.getPlayer().getUniqueId());
		if (u != null && u.isServer()) {
			event.setCancelled(!ServerMode.getInstance().isWithinRange(u, event.getBlock()));
		}
	}
}
