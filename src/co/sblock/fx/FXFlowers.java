package co.sblock.fx;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import co.sblock.users.OnlineUser;

public class FXFlowers extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXFlowers() {
		super("FLOWERS", false, 2500, 0, PlayerMoveEvent.class);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void getEffect(OnlineUser u, Class<? extends Event> e) {
		Player p = u.getPlayer();
		Location loc = p.getLocation();
		Block standingIn = loc.getBlock();
		Block standingOn = standingIn.getRelative(BlockFace.DOWN);

		HashMap<String, SblockFX> inHand = FXManager.getInstance().itemScan(p.getItemInHand());
		if (!inHand.containsKey(this.canonicalName) || standingIn.getType() != Material.AIR
				|| (standingOn.getType() != Material.DIRT && standingOn.getType() != Material.GRASS)) {
			return;
		}

		int flowerNum = (int) (Math.random() * 14);
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
			// TODO Keiko, double plants also need to check face block, plenty of passable blocks.
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

	@Override
	public void removeEffect(OnlineUser u) {
		return;
	}
}
