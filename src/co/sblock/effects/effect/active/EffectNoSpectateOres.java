package co.sblock.effects.effect.active;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.Sblock;

/**
 * Effect for removing all ores adjacent to a mined block.
 * 
 * @author Jikoo
 */
public class EffectNoSpectateOres extends EffectAdjacentBlockPlacement {

	public EffectNoSpectateOres(Sblock plugin) {
		super(plugin, Integer.MAX_VALUE, "Unlucky");
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		BlockBreakEvent breakEvent = (BlockBreakEvent) event;
		handleAdjacentBlock(breakEvent.getPlayer(), breakEvent.getBlock());
		super.handleEvent(event, entity, level);
	}

	@Override
	protected void handleAdjacentBlock(Player player, Block block) {
		if (block.getType() == Material.QUARTZ_ORE) {
			block.setType(Material.NETHERRACK);
			return;
		}
		if (block.getType().name().endsWith("_ORE")) {
			block.setType(Material.STONE);
		}
	}

}
