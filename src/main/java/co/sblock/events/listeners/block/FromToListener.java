package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockFromToEvents.
 * 
 * @author Jikoo
 */
public class FromToListener extends SblockListener {

	private final Machines machines;

	public FromToListener(Sblock plugin) {
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
