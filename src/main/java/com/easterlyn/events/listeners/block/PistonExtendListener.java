package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import com.easterlyn.utilities.tuple.Pair;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;

/**
 * Listener for BlockPistonExtendEvents.
 * 
 * @author Jikoo
 */
public class PistonExtendListener extends EasterlynListener {

	private final Machines machines;

	public PistonExtendListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for pistons pushing blocks.
	 * 
	 * @param event the BlockPistonExtendEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPush(BlockPistonExtendEvent event) {
		for (Block block : event.getBlocks()) {
			Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(block);
			if (pair != null) {
				event.setCancelled(pair.getLeft().handlePush(event, pair.getRight()));
			}
		}
	}

}
