package co.sblock.effects.fx;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.events.event.SblockBreakEvent;
import co.sblock.users.OnlineUser;

/**
 * Base for multiple block break behaviours that affect adjacent blocks.
 * 
 * @author Jikoo
 */
public abstract class FXAdjacentBlockModifier extends SblockFX {

	private final BlockFace[] faces;
	@SuppressWarnings("unchecked")
	public FXAdjacentBlockModifier(String name, int cost, int cooldown) {
		super(name, false, cost, cooldown, BlockBreakEvent.class, SblockBreakEvent.class);
		faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
				BlockFace.EAST, BlockFace.WEST };
	}

	@Override
	protected void getEffect(OnlineUser user, Event event) {
		BlockBreakEvent breakEvent = (BlockBreakEvent) event;
		handleAdjacentBlocks(breakEvent.getPlayer(), breakEvent.getBlock());
	}

	private void handleAdjacentBlocks(Player player, Block center) {
		for (BlockFace face : faces) {
			handleAdjacentBlock(player, center.getRelative(face));
		}
	}

	protected abstract void handleAdjacentBlock(Player player, Block block);

	protected boolean handleBlockSet(Player player, Block block, Material toMaterial) {
		// Capture state and change block - prevents certain plugins assuming block being placed is of the replaced material
		BlockState state = block.getState();
		block.setType(toMaterial);
		BlockPlaceEvent event = new BlockPlaceEvent(block, state, block, new ItemStack(toMaterial), player, true);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			// Revert block changes if cancelled
			state.update(true, false);
			return false;
		}
		return true;
	}

	@Override
	public void removeEffect(OnlineUser user) {}
}
