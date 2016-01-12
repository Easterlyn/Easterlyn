package co.sblock.events;

import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import co.sblock.Sblock;
import co.sblock.utilities.HashQueue;

/**
 * Manager for queuing block updates to prevent redundant block updates by Effects such as Tunnel
 * Bore and Liquid Cooled.
 * 
 * @author Jikoo
 */
public class BlockUpdateManager {

	private final Sblock plugin;
	private final Queue<Block> pending;
	private final BlockFace[] surrounding;
	private BukkitTask queueDrain;

	public BlockUpdateManager(Sblock plugin) {
		this.plugin = plugin;
		this.pending = new HashQueue<>();
		this.surrounding = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
				BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
	}

	public void queueSurroundingBlocks(Block block) {
		for (BlockFace face : surrounding) {
			Block relative = block.getRelative(face);
			if (relative.getType() != Material.AIR) {
				pending.add(relative);
			}
		}
		startTask();
	}

	public void queueBlock(Block block) {
		if (block.getType() != Material.AIR) {
			pending.add(block);
		}
		startTask();
	}

	private void startTask() {
		if (queueDrain == null) {
			queueDrain = new QueueDrainRunnable().runTaskTimer(plugin, 0, 1L);
		}
	}

	private class QueueDrainRunnable extends BukkitRunnable {
		@Override
		public void run() {
			for (int i = 0; i < 50 && !pending.isEmpty(); i++) {
				Block block = pending.poll();
				if (!block.isEmpty()) {
					block.getState().update(false);
				}
			}
			if (pending.isEmpty()) {
				this.cancel();
				queueDrain = null;
			}
		}
	}

}
