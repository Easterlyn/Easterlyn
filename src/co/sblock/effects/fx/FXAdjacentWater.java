package co.sblock.effects.fx;

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

	@SuppressWarnings("deprecation")
	protected void handleAdjacentBlock(Player player, Block block) {
		if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
			if ((block.getData() == 0 && handleBlockSet(player, block, Material.OBSIDIAN))
					|| (block.getData() != 0 && handleBlockSet(player, block, Material.COBBLESTONE))) {
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
