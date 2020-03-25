package com.easterlyn.util.wrapper;

import com.easterlyn.util.CoordinateUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A much faster map implementation for storing data by block.
 *
 * @param <T> the data to store
 *
 * @author Jikoo
 */
public class BlockMap<T> {

	private final Map<String, TreeMap<Integer, TreeMap<Integer, Map<Integer, T>>>> serverMap = new HashMap<>();

	public @Nullable T put(@NotNull Block block, @Nullable T value) {
		TreeMap<Integer, TreeMap<Integer, Map<Integer, T>>> worldMap = serverMap.computeIfAbsent(block.getWorld().getName(), k -> new TreeMap<>());
		TreeMap<Integer, Map<Integer, T>> blockXMap = worldMap.computeIfAbsent(block.getX(), k -> new TreeMap<>());
		Map<Integer, T> blockZMap = blockXMap.computeIfAbsent(block.getZ(), (blockZ) -> new HashMap<>());

		return blockZMap.put(block.getY(), value);
	}

	public @Nullable T get(@NotNull Block block) {
		TreeMap<Integer, TreeMap<Integer, Map<Integer, T>>> worldMap = serverMap.get(block.getWorld().getName());
		if (worldMap == null) {
			return null;
		}

		TreeMap<Integer, Map<Integer, T>> blockXMap = worldMap.get(block.getX());
		if (blockXMap == null) {
			return null;
		}

		Map<Integer, T> blockZMap = blockXMap.get(block.getZ());
		if (blockZMap == null) {
			return null;
		}

		return blockZMap.get(block.getY());
	}

	public @Nullable T remove(@NotNull Block block) {
		TreeMap<Integer, TreeMap<Integer, Map<Integer, T>>> worldMap = serverMap.get(block.getWorld().getName());
		if (worldMap == null) {
			return null;
		}

		TreeMap<Integer, Map<Integer, T>> blockXMap = worldMap.get(block.getX());
		if (blockXMap == null) {
			return null;
		}

		Map<Integer, T> blockZMap = blockXMap.get(block.getZ());
		if (blockZMap == null) {
			return null;
		}

		T value = blockZMap.remove(block.getY());

		if (blockZMap.isEmpty()) {
			blockXMap.remove(block.getZ());
			if (blockXMap.isEmpty()) {
				worldMap.remove(block.getX());
				if (worldMap.isEmpty()) {
					serverMap.remove(block.getWorld().getName());
				}
			}
		}

		return value;
	}

	public @NotNull Collection<T> get(@NotNull Chunk chunk) {
		return get(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	public @NotNull Collection<T> get(@NotNull String world, int chunkX, int chunkZ) {
		TreeMap<Integer, TreeMap<Integer, Map<Integer, T>>> worldMap = serverMap.get(world);
		if (worldMap == null) {
			return Collections.emptyList();
		}

		int blockXMin = CoordinateUtil.chunkToBlock(chunkX);
		SortedMap<Integer, TreeMap<Integer, Map<Integer, T>>> chunkXSubMap = worldMap.subMap(blockXMin, blockXMin + 16);
		if (chunkXSubMap.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> values = new ArrayList<>();
		int blockZMin = CoordinateUtil.chunkToBlock(chunkZ);
		for (Map.Entry<Integer, TreeMap<Integer, Map<Integer, T>>> blockXEntry : chunkXSubMap.entrySet()) {
			SortedMap<Integer, Map<Integer, T>> chunkZSubMap = blockXEntry.getValue().subMap(blockZMin, blockZMin + 16);

			for (Map<Integer, T> blockYMap : chunkZSubMap.values()) {
				values.addAll(blockYMap.values());
			}
		}

		return values;
	}

	public @NotNull Collection<T> remove(@NotNull Chunk chunk) {
		return remove(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	public @NotNull Collection<T> remove(@NotNull String world, int chunkX, int chunkZ) {
		TreeMap<Integer, TreeMap<Integer, Map<Integer, T>>> worldMap = serverMap.get(world);
		if (worldMap == null) {
			return Collections.emptyList();
		}

		int blockXMin = CoordinateUtil.chunkToBlock(chunkX);
		SortedMap<Integer, TreeMap<Integer, Map<Integer, T>>> chunkXSubMap = worldMap.subMap(blockXMin, blockXMin + 16);
		if (chunkXSubMap.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> values = new ArrayList<>();
		int blockZMin = CoordinateUtil.chunkToBlock(chunkZ);

		for (Iterator<TreeMap<Integer, Map<Integer, T>>> blockXIterator = chunkXSubMap.values().iterator(); blockXIterator.hasNext(); ) {
			TreeMap<Integer, Map<Integer, T>> blockXValue = blockXIterator.next();
			SortedMap<Integer, Map<Integer, T>> chunkZSubMap = blockXValue.subMap(blockZMin, blockZMin + 16);

			for (Iterator<Map<Integer, T>> blockZIterator = chunkZSubMap.values().iterator(); blockZIterator.hasNext(); ) {
				values.addAll(blockZIterator.next().values());
				blockZIterator.remove();
			}

			if (blockXValue.isEmpty()) {
				blockXIterator.remove();
			}
		}

		if (worldMap.isEmpty()) {
			serverMap.remove(world);
		}

		return values;
	}

	/**
	 * Gets a collection of entries.
	 * <p>
	 *     N.B. This ignores any entries that do not currently have a loaded world!
	 * </p>
	 *
	 * @return a collection of entries
	 */
	@NotNull
	public Collection<Map.Entry<Block, T>> entrySet() {
		List<Map.Entry<Block, T>> entries = new ArrayList<>();

		this.serverMap.forEach((worldName, worldMap) -> {

			World world = Bukkit.getWorld(worldName);
			if (world == null) {
				return;
			}

			worldMap.forEach((x, xMap) -> xMap.forEach((z, zMap) -> zMap.forEach((y, value) -> {
				entries.add(new Map.Entry<Block, T>() {
					@Override
					public Block getKey() {
						return world.getBlockAt(x, y, z);
					}

					@Override
					public T getValue() {
						return value;
					}

					@Override
					public T setValue(Object value) {
						throw new UnsupportedOperationException("Modification of mappings not allowed here!");
					}
				});
			})));
		});

		return entries;
	}

}
