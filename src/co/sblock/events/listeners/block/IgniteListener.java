package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockIgniteEvents.
 * 
 * @author Jikoo
 */
public class IgniteListener implements Listener {

	/**
	 * EventHandler for BlockIgniteEvents.
	 * 
	 * @param event the BlockIgniteEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleIgnite(event, pair.getRight()));
		}
	}
}
