package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFadeEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockFadeEvents.
 * 
 * @author Jikoo
 */
public class FadeListener extends SblockListener {

	private final Machines machines;

	public FadeListener(Sblock plugin) {
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
