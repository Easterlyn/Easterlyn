package com.easterlyn.events;

import com.easterlyn.Easterlyn;
import net.minecraft.server.v1_13_R2.BlockPosition;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manager for queuing block updates to prevent redundant block updates by Effects such as Tunnel
 * Bore and Liquid Cooled.
 *
 * @author Jikoo
 */
public class BlockUpdateManager {

	private final Easterlyn plugin;
	private final Map<Block, Block> pending;
	private BukkitTask queueDrain;
	private final BlockFace[] adjacent;

	BlockUpdateManager(Easterlyn plugin) {
		this.plugin = plugin;
		this.pending = new LinkedHashMap<>();
		this.adjacent = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
				BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
	}

	public void queueBlock(Block block) {
		for (BlockFace face : adjacent) {
			Block relative = block.getRelative(face);
			if (!relative.isEmpty()) {
				this.pending.put(relative, block);
			}
		}
		this.pending.put(block, block);
		startTask();
	}

	private void startTask() {
		if (queueDrain == null) {
			queueDrain = new QueueDrainRunnable().runTaskTimer(plugin, 0, 1L);
		}
	}

	private class QueueDrainRunnable extends BukkitRunnable {
		@SuppressWarnings("deprecation")
		@Override
		public void run() {

			Iterator<Map.Entry<Block, Block>> pendingIterator = pending.entrySet().iterator();

			for (int i = 0; i < 50 && pendingIterator.hasNext(); i++) {
				// Sadly, using the API does not work.
				// Blocks that are currently air cannot be updated at all to fix adjacent blocks,
				// and certain other edge cases also will not trigger updates.
				// Instead, we manually force an update using NMS.

				Map.Entry<Block, Block> nextPending = pendingIterator.next();
				pendingIterator.remove();
				Block block = nextPending.getValue();
				BlockPosition position2 = new BlockPosition(block.getX(), block.getY(), block.getZ());
				block = nextPending.getKey();
				BlockPosition position1 = new BlockPosition(block.getX(), block.getY(), block.getZ());
				((CraftWorld) block.getWorld()).getHandle()
						.a(position1, CraftMagicNumbers.getBlock(block.getType(), block.getData()).getBlock(), position2);
			}
			if (pending.isEmpty()) {
				this.cancel();
				queueDrain = null;
			}
		}
	}

}
