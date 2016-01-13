package co.sblock.effects.effect.passive;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.Effect;

/**
 * Flowers while you walk!
 * 
 * @author Dublekfx, Jikoo
 */
public class EffectFlowers extends Effect implements BehaviorPassive {

	public EffectFlowers(Sblock plugin) {
		super(plugin, 1200, 1, 1, "Flowers");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void applyEffect(LivingEntity entity, int level) {
		Block standingIn = entity.getLocation().getBlock();
		Block standingOn = standingIn.getRelative(BlockFace.DOWN);

		if (!standingIn.isEmpty()
				|| (standingOn.getType() != Material.DIRT && standingOn.getType() != Material.GRASS)) {
			return;
		}

		Block head = standingIn.getRelative(BlockFace.UP);

		int flowerNum = (int) (head.isEmpty() ? Math.random() * 10 : Math.random() * 14);
		switch(flowerNum) {
		case 0:
			standingIn.setType(Material.YELLOW_FLOWER);
			break;
		case 1:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 0, true);
			break;
		case 2:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 1, true);
			break;
		case 3:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 2, true);
			break;
		case 4:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 3, true);
			break;
		case 5:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 4, true);
			break;
		case 6:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 5, true);
			break;
		case 7:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 6, true);
			break;
		case 8:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 7, true);
			break;
		case 9:
			standingIn.setTypeIdAndData(Material.RED_ROSE.getId(), (byte) 8, true);
			break;
		case 10:
			standingIn.setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 0, true);
			standingIn.getRelative(BlockFace.UP).setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 8, true);
			break;
		case 11:
			standingIn.setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 1, true);
			standingIn.getRelative(BlockFace.UP).setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 8, true);
			break;
		case 12:
			standingIn.setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 4, true);
			standingIn.getRelative(BlockFace.UP).setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 8, true);
			break;
		case 13:
			standingIn.setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 5, true);
			standingIn.getRelative(BlockFace.UP).setTypeIdAndData(Material.DOUBLE_PLANT.getId(), (byte) 8, true);
			break;
		}
	}

}
