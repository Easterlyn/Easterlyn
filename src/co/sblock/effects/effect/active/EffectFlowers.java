package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;
import co.sblock.effects.effect.EffectBehaviorCooldown;

/**
 * Flowers while you walk!
 * 
 * @author Dublekfx, Jikoo
 */
public class EffectFlowers extends Effect implements EffectBehaviorActive, EffectBehaviorCooldown {

	public EffectFlowers() {
		super(1200, 1, 1, "Flowers");
	}

	@Override
	public String getCooldownName() {
		return "Effect:Flowers";
	}

	@Override
	public long getCooldownDuration() {
		return 1000;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(PlayerMoveEvent.class);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void handleEvent(Event event, Player player, int level) {
		Block standingIn = player.getLocation().getBlock();
		Block standingOn = standingIn.getRelative(BlockFace.DOWN);

		if (standingIn.getType() != Material.AIR
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
