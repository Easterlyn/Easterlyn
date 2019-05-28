package com.easterlyn.effects.effect.passive;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorPassive;
import com.easterlyn.effects.effect.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Flowers while you walk!
 *
 * @author Dublekfx, Jikoo
 */
public class EffectFlowers extends Effect implements BehaviorPassive {

	public EffectFlowers(Easterlyn plugin) {
		super(plugin, 1200, 1, 1, "Flowers");
	}

	@Override
	public void applyEffect(LivingEntity entity, int level) {
		Block standingIn = entity.getLocation().getBlock();
		Block standingOn = standingIn.getRelative(BlockFace.DOWN);

		if (!standingIn.isEmpty()
				|| (standingOn.getType() != Material.DIRT && standingOn.getType() != Material.GRASS)) {
			return;
		}

		Block head = standingIn.getRelative(BlockFace.UP);

		switch(ThreadLocalRandom.current().nextInt(head.isEmpty() ? 10 : 14)) {
		case 0:
			standingIn.setType(Material.DANDELION);
			break;
		case 1:
			standingIn.setType(Material.POPPY);
			break;
		case 2:
			standingIn.setType(Material.BLUE_ORCHID);
			break;
		case 3:
			standingIn.setType(Material.ALLIUM);
			break;
		case 4:
			standingIn.setType(Material.AZURE_BLUET);
			break;
		case 5:
			standingIn.setType(Material.RED_TULIP);
			break;
		case 6:
			standingIn.setType(Material.ORANGE_TULIP);
			break;
		case 7:
			standingIn.setType(Material.WHITE_TULIP);
			break;
		case 8:
			standingIn.setType(Material.PINK_TULIP);
			break;
		case 9:
			standingIn.setType(Material.OXEYE_DAISY);
			break;
		case 10:
			// TODO: does this work?
			standingIn.setType(Material.SUNFLOWER);
			head.setType(Material.SUNFLOWER);
			((Bisected) head.getBlockData()).setHalf(Bisected.Half.TOP);
			break;
		case 11:
			standingIn.setType(Material.LILAC);
			head.setType(Material.LILAC);
			((Bisected) head.getBlockData()).setHalf(Bisected.Half.TOP);
			break;
		case 12:
			standingIn.setType(Material.ROSE_BUSH);
			head.setType(Material.ROSE_BUSH);
			((Bisected) head.getBlockData()).setHalf(Bisected.Half.TOP);
			break;
		case 13:
			standingIn.setType(Material.PEONY);
			head.setType(Material.PEONY);
			((Bisected) head.getBlockData()).setHalf(Bisected.Half.TOP);
			break;
		}
	}

}
