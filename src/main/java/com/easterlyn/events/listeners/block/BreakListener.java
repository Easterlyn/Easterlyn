package com.easterlyn.events.listeners.block;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.Effects;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.micromodules.Spectators;

import com.easterlyn.utilities.tuple.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Listener for BlockBreakEvents.
 *
 * @author Jikoo
 */
public class BreakListener extends EasterlynListener {

	private final Effects effects;
	private final Machines machines;
	private final Spectators spectators;
	private final BehaviorActive spectatorsDeserveFun;

	public BreakListener(Easterlyn plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
		this.machines = plugin.getModule(Machines.class);
		this.spectators = plugin.getModule(Spectators.class);
		this.spectatorsDeserveFun = (BehaviorActive) effects.getEffect("Unlucky");
	}

	/**
	 * The event handler for Machine deconstruction.
	 *
	 * @param event the BlockBreakEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleBreak(event, pair.getRight()));
			return;
		}

		if (!spectators.canMineOre(event.getPlayer())) {
			spectatorsDeserveFun.handleEvent(event, event.getPlayer(), 1);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreakOccurred(BlockBreakEvent event) {
		effects.handleEvent(event, event.getPlayer(), false);
		effects.handleEvent(event, event.getPlayer(), true);
	}

}
