package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFadeEvent;

/**
 * Listener for BlockFadeEvents.
 * 
 * @author Jikoo
 */
public class FadeListener extends EasterlynListener {

	private final Machines machines;

	public FadeListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for BlockFadeEvents.
	 * 
	 * @param event the BlockFadeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event) {
		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleFade(event, pair.getRight()));
		}
	}

}
