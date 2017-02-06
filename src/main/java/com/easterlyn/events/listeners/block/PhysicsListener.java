package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * Listener for BlockPhysicsEvents.
 * 
 * @author Jikoo
 */
public class PhysicsListener extends EasterlynListener {

	private final Machines machines;

	public PhysicsListener(Easterlyn plugin) {
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
