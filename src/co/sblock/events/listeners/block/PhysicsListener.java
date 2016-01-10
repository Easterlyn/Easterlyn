package co.sblock.events.listeners.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPhysicsEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;

/**
 * Listener for BlockPhysicsEvents.
 * 
 * @author Jikoo
 */
public class PhysicsListener extends SblockListener {

	private final Machines machines;

	public PhysicsListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for BlockPhysicsEvents.
	 * 
	 * @param event the BlockPhysicsEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (machines.isMachine(event.getBlock())) {
			event.setCancelled(true);
		}
	}
}
