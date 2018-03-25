package com.easterlyn.effects.effect.active;

import com.easterlyn.Easterlyn;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Automatically change adjacent lava to obsidian or cobblestone and extinguish fires when mining.
 *
 * @author Jikoo
 */
public class EffectAdjacentWater extends EffectAdjacentBlockPlacement { // TODO godtier: fire

	public EffectAdjacentWater(Easterlyn plugin) {
		super(plugin, 400, "Liquid Cooled", Material.STATIONARY_LAVA);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean handleAdjacentBlock(Player player, Block block) {
		if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
			if (this.currentCount > 0 && block.getWorld().getEnvironment() == Environment.NETHER) {
				return true;
			}
			if (((block.getData() == 0 && handleBlockSet(player, block, Material.OBSIDIAN))
					|| (block.getData() != 0 && handleBlockSet(player, block, Material.COBBLESTONE)))
					&& this.currentCount == 0) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.BLOCK_LAVA_EXTINGUISH, 4, 1);
			}
			return true;
		}
		if (block.getType() == Material.FIRE) {
			if (handleBlockSet(player, block, Material.AIR) && this.currentCount == 0) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.BLOCK_FIRE_EXTINGUISH, 4, 1);
			}
			return true;
		}
		return false;
	}

}
