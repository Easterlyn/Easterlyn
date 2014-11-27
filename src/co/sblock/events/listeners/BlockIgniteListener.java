package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockIgniteEvents.
 * 
 * @author Jikoo
 */
public class BlockIgniteListener implements Listener {

	/**
	 * EventHandler for BlockIgniteEvents.
	 * 
	 * @param event the BlockIgniteEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Machine m = SblockMachines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleIgnite(event));
		}
	}
}
