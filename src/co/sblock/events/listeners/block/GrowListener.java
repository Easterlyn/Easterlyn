package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockGrowEvents.
 * 
 * @author Jikoo
 */
public class GrowListener extends SblockListener {

	private final Machines machines;

	public GrowListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * An event handler for a change that is caused by or affects a Block in a
	 * Machine.
	 * 
	 * @param event the BlockGrowEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent event) {
		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleGrow(event, pair.getRight()));
		}
	}
}
