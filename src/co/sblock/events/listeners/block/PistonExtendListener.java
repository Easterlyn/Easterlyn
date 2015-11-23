package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockPistonExtendEvents.
 * 
 * @author Jikoo
 */
public class PistonExtendListener extends SblockListener {

	private final Machines machines;

	public PistonExtendListener(Sblock plugin) {
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
