package com.easterlyn.events;

import com.easterlyn.Easterlyn;
import com.easterlyn.utilities.HashQueue;
import net.minecraft.server.v1_12_R1.BlockPosition;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Queue;

/**
 * Manager for queuing block updates to prevent redundant block updates by Effects such as Tunnel
 * Bore and Liquid Cooled.
 *
 * @author Jikoo
 */
public class BlockUpdateManager {

	private final Easterlyn plugin;
	private final Queue<Block> pending;
	private BukkitTask queueDrain;
	private final BlockFace[] adjacent;

	BlockUpdateManager(Easterlyn plugin) {
		this.plugin = plugin;
		this.pending = new HashQueue<>();
		this.adjacent = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
				BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
	}

	public void queueBlock(Block block) {
		// Only update blocks with adjacent non-air blocks
		if (block.getType() == Material.AIR) {
			boolean update = false;
			for (BlockFace face : adjacent) {
				update = block.getRelative(face).getType() != Material.AIR;
				if (update) {
					break;
				}
			}
			if (!update) {
				return;
			}
		}
		pending.add(block);
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
			for (int i = 0; i < 50 && !pending.isEmpty(); i++) {
				// Sadly, using the API does not work: pending.poll().getState().update(true, true);
				// Blocks that are currently air cannot be updated at all to fix adjacent blocks,
				// and certain other edge cases also will not trigger updates.
				// Instead, we manually force an update using NMS.
				// TODO 1.12: This now updates surrounding blocks, much heavier. Re-evaluate and figure out individual updates again.
				Block block = pending.poll();
				((CraftWorld) block.getWorld()).getHandle().applyPhysics(
						new BlockPosition(block.getX(), block.getY(), block.getZ()),
						CraftMagicNumbers.getBlock(block).fromLegacyData(block.getData()).getBlock(), false);
			}
			if (pending.isEmpty()) {
				this.cancel();
				queueDrain = null;
			}
		}
	}

}