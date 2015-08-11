package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockPistonRetractEvents.
 * 
 * @author Jikoo
 */
public class PistonRetractListener implements Listener {

	/**
	 * EventHandler for when a block is pulled by a sticky piston.
	 * 
	 * @param event the BlockPistonRetractEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockPull(BlockPistonRetractEvent event) {
		for (Block block : event.getBlocks()) {
			Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(block);
			if (pair != null) {
				event.setCancelled(pair.getLeft().handlePull(event, pair.getRight()));
			}
		}
	}
}
