package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.Effect;
import co.sblock.events.event.SblockBreakEvent;

/**
 * Base for multiple block break behaviors that affect adjacent blocks.
 * 
 * @author Jikoo
 */
public abstract class EffectAdjacentBlockModifier extends Effect implements BehaviorActive {

	private final BlockFace[] faces;

	protected EffectAdjacentBlockModifier(Sblock plugin, int cost, String name) {
		super(plugin, cost, 1, 1, name);
		faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
				BlockFace.EAST, BlockFace.WEST };
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(BlockBreakEvent.class, SblockBreakEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		BlockBreakEvent breakEvent = (BlockBreakEvent) event;
		Player player = breakEvent.getPlayer();
		for (BlockFace face : faces) {
			handleAdjacentBlock(player, breakEvent.getBlock().getRelative(face));
		}
	}

	protected abstract void handleAdjacentBlock(Player player, Block block);
}
