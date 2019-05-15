package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import com.easterlyn.utilities.tuple.Pair;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonRetractEvent;

/**
 * Listener for BlockPistonRetractEvents.
 * 
 * @author Jikoo
 */
public class PistonRetractListener extends EasterlynListener {

	private final Machines machines;

	public PistonRetractListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for when a block is pulled by a sticky piston.
	 * 
	 * @param event the BlockPistonRetractEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPull(BlockPistonRetractEvent event) {
		for (Block block : event.getBlocks()) {
			Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(block);
			if (pair != null) {
				event.setCancelled(pair.getLeft().handlePull(event, pair.getRight()));
			}
		}
	}

}
