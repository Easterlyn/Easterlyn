package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;

/**
 * Automatically change adjacent lava to obsidian or cobblestone and extinguish fires when mining.
 *
 * @author Jikoo
 */
public class EffectAdjacentWater extends EffectAdjacentBlockPlacement {

	public EffectAdjacentWater(EasterlynEffects plugin) {
		super(plugin, "Liquid Cooled", 400);
	}

	@Override
	protected boolean handleAdjacentBlock(Player player, Block block, int currentCount) {
		if (block.getType() == Material.LAVA) {
			if (currentCount > 0 && block.getWorld().getEnvironment() == Environment.NETHER) {
				return false;
			}
			BlockData blockData = block.getBlockData();
			if (!(blockData instanceof Levelled)) {
				return false;
			}
			int level = ((Levelled) blockData).getLevel();
			if (((level == 0 && handleBlockSet(player, block, Material.OBSIDIAN))
					|| (level != 0 && handleBlockSet(player, block, Material.COBBLESTONE)))
					&& currentCount == 0) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.BLOCK_LAVA_EXTINGUISH,
						SoundCategory.BLOCKS, 4, 1);
			}
			return true;
		}
		if (block.getType() == Material.FIRE) {
			if (handleBlockSet(player, block, Material.AIR) && currentCount == 0) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.BLOCK_FIRE_EXTINGUISH,
						SoundCategory.BLOCKS, 4, 1);
			}
			return true;
		}
		return false;
	}

}
