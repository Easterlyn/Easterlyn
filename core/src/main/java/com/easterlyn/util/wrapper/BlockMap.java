package com.easterlyn.util.wrapper;

import com.easterlyn.util.CoordinateUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A much faster map implementation for storing data by block.
 *
 * @param <V> the data to store
 *
 * @author Jikoo
 */
public class BlockMap<V> {

	private final Map<String, TreeMap<Integer, TreeMap<Integer, Map<Integer, V>>>> serverMap = new HashMap<>();

	@Nullable
	public V put(@NotNull Block block, @Nullable V value) {
		CompletableFuture<V> future = new CompletableFuture<>();

		this.serverMap.compute(block.getWorld().getName(), (worldName, worldMap) -> {
			if (worldMap == null) {
				worldMap = new TreeMap<>();
			}

			worldMap.compute(block.getX(), (blockX, blockXMap) -> {
				if (blockXMap == null) {
					blockXMap = new TreeMap<>();
				}

				blockXMap.compute(block.getZ(), (blockZ, blockZMap) -> {
					if (blockZMap == null) {
						blockZMap = new HashMap<>();
					}

					future.complete(blockZMap.put(block.getY(), value));

					return blockZMap.isEmpty() ? null : blockZMap;
				});

				return blockXMap.isEmpty() ? null : blockXMap;
			});

			return worldMap.isEmpty() ? null : worldMap;
		});

		return future.getNow(null);
	}

	@Nullable
	public V get(@NotNull Block block) {
		CompletableFuture<V> future = new CompletableFuture<>();

		this.serverMap.computeIfPresent(block.getWorld().getName(), (worldName, worldMap) -> {
			worldMap.computeIfPresent(block.getX(), (blockX, blockXMap) -> {
				blockXMap.computeIfPresent(block.getZ(), (blockZ, blockZMap) -> {
					future.complete(blockZMap.get(block.getY()));
					return blockZMap.isEmpty() ? null : blockZMap;
				});
				return blockXMap.isEmpty() ? null : blockXMap;
			});
			return worldMap.isEmpty() ? null : worldMap;
		});

		return future.getNow(null);
	}

	@Nullable
	public V remove(@NotNull Block block) {
		CompletableFuture<V> future = new CompletableFuture<>();

		this.serverMap.computeIfPresent(block.getWorld().getName(), (worldName, worldMap) -> {
			worldMap.computeIfPresent(block.getX(), (blockX, blockXMap) -> {
				blockXMap.computeIfPresent(block.getZ(), (blockZ, blockZMap) -> {
					future.complete(blockZMap.remove(block.getY()));
					return blockZMap.isEmpty() ? null : blockZMap;
				});
				return blockXMap.isEmpty() ? null : blockXMap;
			});
			return worldMap.isEmpty() ? null : worldMap;
		});

		return future.getNow(null);
	}

	@NotNull
	public Collection<V> get(@NotNull Chunk chunk) {
		return get(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	@NotNull
	public Collection<V> get(@NotNull String world, int chunkX, int chunkZ) {
		List<V> values = new ArrayList<>();

		this.serverMap.computeIfPresent(world, (worldName, worldMap) -> {
			int blockXMin = CoordinateUtil.chunkToBlock(chunkX);
			Map<Integer, TreeMap<Integer, Map<Integer, V>>> blockXMap = worldMap.subMap(blockXMin, blockXMin + 16);

			blockXMap.entrySet().removeIf(blockXMapping -> {
				int blockZMin = CoordinateUtil.chunkToBlock(chunkZ);
				Map<Integer, Map<Integer, V>> blockZMap = blockXMapping.getValue().subMap(blockZMin, blockZMin + 16);

				blockZMap.entrySet().removeIf(blockZMapping -> {
					values.addAll(blockZMapping.getValue().values());
					return blockZMapping.getValue().isEmpty();
				});

				return blockXMapping.getValue().isEmpty();
			});

			return worldMap.isEmpty() ? null : worldMap;
		});

		return values;
	}

	@NotNull
	public Collection<V> remove(@NotNull Chunk chunk) {
		return remove(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	@NotNull
	public Collection<V> remove(@NotNull String world, int chunkX, int chunkZ) {
		List<V> values = new ArrayList<>();

		this.serverMap.computeIfPresent(world, (worldName, worldMap) -> {
			int blockXMin = CoordinateUtil.chunkToBlock(chunkX);
			Map<Integer, TreeMap<Integer, Map<Integer, V>>> blockXMap = worldMap.subMap(blockXMin, blockXMin + 16);

			blockXMap.entrySet().removeIf(blockXMapping -> {
				int blockZMin = CoordinateUtil.chunkToBlock(chunkZ);
				Map<Integer, Map<Integer, V>> blockZMap = blockXMapping.getValue().subMap(blockZMin, blockZMin + 16);

				blockZMap.entrySet().removeIf(blockZMapping -> {
					values.addAll(blockZMapping.getValue().values());
					blockZMapping.getValue().clear();
					return true;
				});

				return blockXMapping.getValue().isEmpty();
			});

			return worldMap.isEmpty() ? null : worldMap;
		});

		return values;
	}

}
