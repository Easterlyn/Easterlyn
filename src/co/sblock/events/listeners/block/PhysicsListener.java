package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPhysicsEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

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
		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handlePhysics(event, pair.getRight()));
		}
	}
}
