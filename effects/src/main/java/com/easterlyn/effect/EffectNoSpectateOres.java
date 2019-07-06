package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Effect for removing all ores adjacent to a mined block.
 *
 * @author Jikoo
 */
public class EffectNoSpectateOres extends EffectAdjacentBlockPlacement {

	public EffectNoSpectateOres(EasterlynEffects plugin) {
		super(plugin, "Unlucky", Integer.MAX_VALUE);
	}

	@Override
	public void applyEffect(@NotNull LivingEntity entity, int level, @Nullable Event event) {
		if (!(event instanceof BlockBreakEvent)) {
			return;
		}
		BlockBreakEvent breakEvent = (BlockBreakEvent) event;
		handleAdjacentBlock(breakEvent.getPlayer(), breakEvent.getBlock(), 0);
		super.applyEffect(entity, level, event);
	}

	@Override
	protected boolean handleAdjacentBlock(Player player, Block block, int currentCount) {
		if (block.getType() == Material.NETHER_QUARTZ_ORE) {
			block.setType(Material.NETHERRACK);
			return false;
		}
		if (block.getType().name().endsWith("_ORE")) {
			block.setType(Material.STONE);
			return false;
		}
		return false;
	}

}
