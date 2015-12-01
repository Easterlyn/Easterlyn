package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.micromodules.Spectators;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for BlockBreakEvents.
 * 
 * @author Jikoo
 */
public class BreakListener extends SblockListener {

	private final Effects effects;
	private final Machines machines;
	private final Spectators spectators;

	public BreakListener(Sblock plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
		this.machines = plugin.getModule(Machines.class);
		this.spectators = plugin.getModule(Spectators.class);
	}

	/**
	 * The event handler for Machine deconstruction.
	 * 
	 * @param event the BlockBreakEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType().name().endsWith("_ORE") && !spectators.canMineOre(event.getPlayer())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot mine ore shortly after exiting spectate mode!");
			return;
		}

		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleBreak(event, pair.getRight()));
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreakOccurred(BlockBreakEvent event) {
		effects.handleEvent(event, event.getPlayer(), false);
	}
}
