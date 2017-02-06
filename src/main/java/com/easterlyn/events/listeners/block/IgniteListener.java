package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;

/**
 * Listener for BlockIgniteEvents.
 * 
 * @author Jikoo
 */
public class IgniteListener extends EasterlynListener {

	private final Machines machines;

	public IgniteListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for BlockIgniteEvents.
	 * 
	 * @param event the BlockIgniteEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleIgnite(event, pair.getRight()));
		}
	}

}
