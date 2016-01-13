package co.sblock.effects.effect.active;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import co.sblock.Sblock;

/**
 * Freeze all adjacent water source blocks. Not useful against the ground without silk touch, but otherwise nice.
 * 
 * @author Jikoo
 */
public class EffectAdjacentFreeze extends EffectAdjacentBlockPlacement {

	public EffectAdjacentFreeze(Sblock plugin) {
		super(plugin, 400, "Eternally Frozen", Material.STATIONARY_WATER);
	}

	@Override
	protected boolean handleAdjacentBlock(Player player, Block block) {
		if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
			if (handleBlockSet(player, block, Material.ICE)) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.SPLASH2, 16, 1);
				return true;
			}
		}
		return false;
	}

}
