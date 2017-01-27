package com.easterlyn.effects.effect.active;

import com.easterlyn.Easterlyn;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Effect for removing all ores adjacent to a mined block.
 * 
 * @author Jikoo
 */
public class EffectNoSpectateOres extends EffectAdjacentBlockPlacement {

	public EffectNoSpectateOres(Easterlyn plugin) {
		super(plugin, Integer.MAX_VALUE, "Unlucky");
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		BlockBreakEvent breakEvent = (BlockBreakEvent) event;
		handleAdjacentBlock(breakEvent.getPlayer(), breakEvent.getBlock());
		super.handleEvent(event, entity, level);
	}

	@Override
	protected boolean handleAdjacentBlock(Player player, Block block) {
		if (block.getType() == Material.QUARTZ_ORE) {
			block.setType(Material.NETHERRACK);
			return true;
		}
		if (block.getType().name().endsWith("_ORE")) {
			block.setType(Material.STONE);
			return true;
		}
		return false;
	}

}
