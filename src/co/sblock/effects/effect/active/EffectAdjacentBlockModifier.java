package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

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

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;
import co.sblock.events.event.SblockBreakEvent;

/**
 * Base for multiple block break behaviors that affect adjacent blocks.
 * 
 * @author Jikoo
 */
public abstract class EffectAdjacentBlockModifier extends Effect implements EffectBehaviorActive {

	private final BlockFace[] faces;
	public EffectAdjacentBlockModifier(int cost, String... names) {
		super(cost, 1, 1, names);
		faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
				BlockFace.EAST, BlockFace.WEST };
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(BlockBreakEvent.class, SblockBreakEvent.class);
	}
	/* (non-Javadoc)
	 * @see co.sblock.effects.effect.EffectBehaviorActive#handleEvent(org.bukkit.event.Event, org.bukkit.entity.Player, int)
	 */
	@Override
	public void handleEvent(Event event, Player player, int level) {
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
}
