package co.sblock.fx;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Freeze all adjacent water source blocks. Not useful against the ground without silk touch, but otherwise nice.
 * 
 * @author Jikoo
 */
public class FXAdjacentFreeze extends FXAdjacentBlockModifier {

	public FXAdjacentFreeze() {
		super("Eternally Frozen", 400, 0);
	}

	@Override
	protected void handleAdjacentBlock(Player player, Block block) {
		if (block.getType() == Material.STATIONARY_WATER) {
			if (handleBlockSet(player, block, Material.ICE)) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.SPLASH2, 16, 1);
			}
			return;
		}
	}
}
