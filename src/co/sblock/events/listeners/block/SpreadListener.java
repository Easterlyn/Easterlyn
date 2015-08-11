package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockSpreadEvents.
 * 
 * @author Jikoo
 */
public class SpreadListener implements Listener {

	/**
	 * EventHandler for BlockSpreadEvents.
	 * 
	 * @param event the BlockSpreadEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleSpread(event, pair.getRight()));
		}
	}
}
