package co.sblock.fx;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import co.sblock.events.event.SblockBreakEvent;
import co.sblock.users.OnlineUser;

/**
 * Mine or dig a 3x3 area at once.
 * 
 * @author Jikoo
 */
public class FXTunnelBore extends SblockFX {

	private final BlockFace[] faces;
	@SuppressWarnings("unchecked")
	public FXTunnelBore() {
		super("Tunnel Bore", false, 2500, 0, BlockBreakEvent.class);
		faces = new BlockFace[] { BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
				BlockFace.SOUTH_WEST, BlockFace.SOUTH, BlockFace.SOUTH_EAST, BlockFace.EAST,
				BlockFace.WEST };
	}

	@Override
	protected void getEffect(OnlineUser user, Event event) {
		if (!(event instanceof BlockBreakEvent) || event instanceof SblockBreakEvent) {
			return;
		}

		BlockBreakEvent breakEvent = (BlockBreakEvent) event;

		if (breakEvent.getPlayer().isSneaking()) {
			// Sneak to mine single blocks
			return;
		}

		Block block = breakEvent.getBlock();
		Player player = breakEvent.getPlayer();

		for (int y = -1; y < 2; y++) {
			if (block.getY() + y <= 0) {
				continue;
			}
			Block relativeCenter = block.getWorld().getBlockAt(block.getLocation().add(0, y, 0));
			if (y != 0) {
				// More efficient than including BlockFace.SELF
				sblockBreak(relativeCenter, player);
			}
			for (BlockFace face : faces) {
				sblockBreak(relativeCenter.getRelative(face), player);
			}
		}
	}

	private void sblockBreak(Block block, Player player) {
		if (block.getType() == Material.BARRIER || block.getType() == Material.BEDROCK
				|| block.getType() == Material.COMMAND || block.getType() == Material.ENDER_PORTAL
				|| block.getType() == Material.ENDER_PORTAL_FRAME
				|| block.getType() == Material.PORTAL || block.isLiquid() || block.isEmpty()) {
			return;
		}

		SblockBreakEvent event = new SblockBreakEvent(block, player);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		block.breakNaturally(player.getItemInHand());
		// TODO fortune is not applied.
	}

	@Override
	public void removeEffect(OnlineUser user) {}
}
