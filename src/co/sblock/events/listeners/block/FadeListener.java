package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockFadeEvents.
 * 
 * @author Jikoo
 */
public class FadeListener implements Listener {

	/**
	 * EventHandler for BlockFadeEvents.
	 * 
	 * @param event the BlockFadeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event) {
		Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleFade(event, pair.getRight()));
		}
	}
}
