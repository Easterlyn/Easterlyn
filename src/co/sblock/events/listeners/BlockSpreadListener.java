package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockSpreadEvents.
 * 
 * @author Jikoo
 */
public class BlockSpreadListener implements Listener {

	/**
	 * EventHandler for BlockSpreadEvents.
	 * 
	 * @param event the BlockSpreadEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		Machine m = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleSpread(event));
		}
	}
}
