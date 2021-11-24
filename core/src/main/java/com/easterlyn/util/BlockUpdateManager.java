package com.easterlyn.util;

import com.easterlyn.EasterlynCore;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

/**
 * Manager for queuing block updates to prevent redundant or excessive block updates.
 *
 * @author Jikoo
 */
public class BlockUpdateManager {

  private final EasterlynCore plugin;
  private final Map<Block, Block> pending;
  private final BlockFace[] adjacent;
  private @Nullable BukkitTask queueDrain;

  public BlockUpdateManager(EasterlynCore plugin) {
    this.plugin = plugin;
    this.pending = new LinkedHashMap<>();
    this.adjacent =
        new BlockFace[] {
          BlockFace.UP,
          BlockFace.DOWN,
          BlockFace.NORTH,
          BlockFace.SOUTH,
          BlockFace.EAST,
          BlockFace.WEST
        };
  }

  public void forceAllUpdates() {
    this.pending.forEach(this::forceUpdate);
    this.pending.clear();
  }

  public void queueBlock(Block block) {
    for (BlockFace face : adjacent) {
      Block relative = block.getRelative(face);
      if (!relative.isEmpty() && !this.pending.containsKey(relative)) {
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

  private void forceUpdate(Block updated, Block triggering) {
    // Sadly, using the API does not work.
    // Blocks that are currently air cannot be updated at all to fix adjacent blocks,
    // and certain other edge cases also will not trigger updates.
    // Instead, we manually force an update using NMS.

    BlockPos position1 = new BlockPos(updated.getX(), updated.getY(), updated.getZ());
    BlockPos position2 =
        new BlockPos(triggering.getX(), triggering.getY(), triggering.getZ());
    Level nmsWorld = ((CraftWorld) updated.getWorld()).getHandle();

    if (position1.equals(position2)) {
      // Primary affected block, update comparators, observers, etc.
      BlockState blockData = nmsWorld.getBlockState(position1);

      // See Chunk#setBlockState
      blockData.onPlace(nmsWorld, position1, blockData, false);

      // See Level#notifyAndUpdatePhysics
      if (blockData.hasAnalogOutputSignal()) {
        nmsWorld.updateNeighbourForOutputSignal(position1, blockData.getBlock());
      }

      int j = 3 & -2;
      CraftWorld world = nmsWorld.getWorld();
      if (world != null) {
        BlockPhysicsEvent event =
            new BlockPhysicsEvent(
                world.getBlockAt(position1.getX(), position1.getY(), position1.getZ()),
                CraftBlockData.fromData(blockData));
        nmsWorld.getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
          return;
        }
      }

      blockData.updateNeighbourShapes(nmsWorld, position1, j);
      blockData.updateIndirectNeighbourShapes(nmsWorld, position1, j);
    }

    // See World#applyPhysics
    nmsWorld.neighborChanged(position1, CraftMagicNumbers.getBlock(updated.getType()), position2);
  }

  private class QueueDrainRunnable extends BukkitRunnable {

    @Override
    public void run() {

      Iterator<Map.Entry<Block, Block>> pendingIterator = pending.entrySet().iterator();

      for (int i = 0; i < 50 && pendingIterator.hasNext(); i++) {

        Map.Entry<Block, Block> nextPending = pendingIterator.next();
        pendingIterator.remove();

        forceUpdate(nextPending.getKey(), nextPending.getValue());
      }
      if (pending.isEmpty()) {
        this.cancel();
        queueDrain = null;
      }
    }
  }
}
