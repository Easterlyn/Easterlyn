package com.easterlyn.util.wrapper;

import com.github.jikoo.planarwrappers.container.BlockMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A much faster Multimap implementation for storing data by block.
 *
 * @param <T> the data to store
 * @author Jikoo
 */
public class BlockMultiMap<T> {

  private final BlockMap<List<T>> blockMap = new BlockMap<>();

  public void put(@NotNull Block block, @NotNull T value) {
    List<T> list = blockMap.get(block);
    if (list == null) {
      list = new ArrayList<>();
      blockMap.put(block, list);
    }
    list.add(value);
  }

  public @Nullable Collection<T> get(@NotNull Block block) {
    List<T> list = blockMap.get(block);
    if (list == null) {
      return null;
    }
    return Collections.unmodifiableCollection(list);
  }

  public @NotNull Collection<T> get(@NotNull Chunk chunk) {
    return get(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
  }

  public @NotNull Collection<T> get(@NotNull String world, int chunkX, int chunkZ) {
    return blockMap.get(world, chunkX, chunkZ).stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public @Nullable Collection<T> remove(@NotNull Block block) {
    List<T> list = blockMap.remove(block);
    if (list == null) {
      return null;
    }
    return Collections.unmodifiableCollection(list);
  }

  public @NotNull Collection<T> remove(@NotNull Chunk chunk) {
    return remove(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
  }

  public @NotNull Collection<T> remove(@NotNull String world, int chunkX, int chunkZ) {
    return blockMap.remove(world, chunkX, chunkZ).stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public Collection<Map.Entry<Block, List<T>>> entrySet() {
    return blockMap.entrySet();
  }
}
