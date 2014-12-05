package co.sblock.events.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockPistonExtendEvents.
 * 
 * @author Jikoo
 */
public class BlockPistonExtendListener implements Listener {

	/**
	 * EventHandler for pistons pushing blocks.
	 * 
	 * @param event the BlockPistonExtendEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPush(BlockPistonExtendEvent event) {
		for (Block b : event.getBlocks()) {
			Machine m = Machines.getInstance().getMachineByBlock(b);
			if (m != null) {
				event.setCancelled(m.handlePush(event));
				return;
			}
		}
	}
}
