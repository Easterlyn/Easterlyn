package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;

/**
 * Listener for BlockGrowEvents.
 * 
 * @author Jikoo
 */
public class GrowListener extends EasterlynListener {

	private final Machines machines;

	public GrowListener(Easterlyn plugin) {
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
