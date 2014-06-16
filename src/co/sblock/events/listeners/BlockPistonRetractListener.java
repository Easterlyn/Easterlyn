package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockPistonRetractEvents.
 * 
 * @author Jikoo
 */
public class BlockPistonRetractListener implements Listener {

	/**
	 * EventHandler for when a block is pulled by a sticky piston.
	 * 
	 * @param event the BlockPistonRetractEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPull(BlockPistonRetractEvent event) {
		Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handlePull(event));
		}
	}
}
