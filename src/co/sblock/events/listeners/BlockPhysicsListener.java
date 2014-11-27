package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockPhysicsEvents.
 * 
 * @author Jikoo
 */
public class BlockPhysicsListener implements Listener {

	/**
	 * EventHandler for BlockPhysicsEvents.
	 * 
	 * @param event the BlockPhysicsEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Machine m = SblockMachines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handlePhysics(event));
		}
	}
}
