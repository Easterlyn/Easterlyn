package com.easterlyn.util.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A much faster Multimap implementation for storing data by block.
 *
 * @param <T> the data to store
 *
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

	@Nullable
	public Collection<T> get(@NotNull Block block) {
		List<T> list = blockMap.get(block);
		if (list == null) {
			return null;
		}
		return Collections.unmodifiableCollection(list);
	}

	@NotNull
	public Collection<T> get(@NotNull Chunk chunk) {
		return get(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	@NotNull
	public Collection<T> get(@NotNull String world, int chunkX, int chunkZ) {
		return blockMap.get(world, chunkX, chunkZ).stream().flatMap(Collection::stream).collect(Collectors.toList());
	}

	@Nullable
	public Collection<T> remove(@NotNull Block block) {
		List<T> list = blockMap.remove(block);
		if (list == null) {
			return null;
		}
		return Collections.unmodifiableCollection(list);
	}

	@NotNull
	public Collection<T> remove(@NotNull Chunk chunk) {
		return remove(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	@NotNull
	public Collection<T> remove(@NotNull String world, int chunkX, int chunkZ) {
		return blockMap.remove(world, chunkX, chunkZ).stream().flatMap(Collection::stream).collect(Collectors.toList());
	}

}
