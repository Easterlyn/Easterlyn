package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import com.easterlyn.utilities.tuple.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 * Listener for BlockSpreadEvents.
 * 
 * @author Jikoo
 */
public class SpreadListener extends EasterlynListener {

	private final Machines machines;

	public SpreadListener(Easterlyn plugin) {
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
