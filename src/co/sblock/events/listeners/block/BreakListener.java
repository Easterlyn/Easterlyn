package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.effects.effect.BehaviorActive;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.micromodules.Spectators;

/**
 * Listener for BlockBreakEvents.
 * 
 * @author Jikoo
 */
public class BreakListener extends SblockListener {

	private final Effects effects;
	private final Machines machines;
	private final Spectators spectators;
	private final BehaviorActive spectatorsDeserveFun;

	public BreakListener(Sblock plugin) {
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
			return;
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreakOccurred(BlockBreakEvent event) {
		effects.handleEvent(event, event.getPlayer(), false);
		effects.handleEvent(event, event.getPlayer(), true);
	}
}
