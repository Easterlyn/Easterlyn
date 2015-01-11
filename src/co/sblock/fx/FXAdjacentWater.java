package co.sblock.fx;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Automatically change adjacent lava to obsidian or cobblestone and extinguish fires when mining.
 * 
 * @author Jikoo
 */
public class FXAdjacentWater extends FXAdjacentBlockModifier {

	public FXAdjacentWater() {
		super("Liquid Cooled", 400, 0);
	}

	protected void handleAdjacentBlock(Player player, Block block) {
		if (block.getType() == Material.LAVA) {
			if (handleBlockSet(player, block, Material.COBBLESTONE)) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.FIZZ, 16, 1);
			}
			return;
		}
		if (block.getType() == Material.STATIONARY_LAVA) {
			if (handleBlockSet(player, block, Material.OBSIDIAN)) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.FIZZ, 16, 1);
			}
			return;
		}
		if (block.getType() == Material.FIRE) {
			if (handleBlockSet(player, block, Material.AIR)) {
				block.getWorld().playSound(block.getLocation().add(.5, 0, .5), Sound.FIZZ, 16, 1);
			}
			return;
		}
	}
}
