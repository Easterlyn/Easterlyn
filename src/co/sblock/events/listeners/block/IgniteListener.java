package co.sblock.events.listeners.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockIgniteEvents.
 * 
 * @author Jikoo
 */
public class IgniteListener implements Listener {

	/**
	 * EventHandler for BlockIgniteEvents.
	 * 
	 * @param event the BlockIgniteEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Machine m = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleIgnite(event));
		}
	}
}
