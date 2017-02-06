package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;

/**
 * Listener for BlockFromToEvents.
 * 
 * @author Jikoo
 */
public class FromToListener extends EasterlynListener {

	private final Machines machines;

	public FromToListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
		Pair<Machine, ConfigurationSection> machine = machines.getMachineByBlock(event.getToBlock());

		if (machine != null) {
			machine.getKey().handleFromTo(event, machine.getRight());
		}
	}

}
