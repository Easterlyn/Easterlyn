package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockSpreadEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockSpreadEvents.
 * 
 * @author Jikoo
 */
public class SpreadListener extends SblockListener {

	private final Machines machines;

	public SpreadListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for BlockSpreadEvents.
	 * 
	 * @param event the BlockSpreadEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleSpread(event, pair.getRight()));
		}
	}
}
