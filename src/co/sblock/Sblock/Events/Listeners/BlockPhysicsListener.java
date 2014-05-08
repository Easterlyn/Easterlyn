package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Machines.Type.Machine;

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
	@EventHandler(ignoreCancelled = false)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handlePhysics(event));
		}
	}
}
