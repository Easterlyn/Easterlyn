package com.easterlyn.util.wrapper;

import com.easterlyn.util.CoordinateUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
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

	@Nullable
	public T put(@NotNull Block block, @Nullable T value) {
		CompletableFuture<T> future = new CompletableFuture<>();

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
	public T get(@NotNull Block block) {
		CompletableFuture<T> future = new CompletableFuture<>();

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
	public T remove(@NotNull Block block) {
		CompletableFuture<T> future = new CompletableFuture<>();

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
	public Collection<T> get(@NotNull Chunk chunk) {
		return get(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	@NotNull
	public Collection<T> get(@NotNull String world, int chunkX, int chunkZ) {
		List<T> values = new ArrayList<>();

		this.serverMap.computeIfPresent(world, (worldName, worldMap) -> {
			int blockXMin = CoordinateUtil.chunkToBlock(chunkX);
			Map<Integer, TreeMap<Integer, Map<Integer, T>>> blockXMap = worldMap.subMap(blockXMin, blockXMin + 16);

			blockXMap.entrySet().removeIf(blockXMapping -> {
				int blockZMin = CoordinateUtil.chunkToBlock(chunkZ);
				Map<Integer, Map<Integer, T>> blockZMap = blockXMapping.getValue().subMap(blockZMin, blockZMin + 16);

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
	public Collection<T> remove(@NotNull Chunk chunk) {
		return remove(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	@NotNull
	public Collection<T> remove(@NotNull String world, int chunkX, int chunkZ) {
		List<T> values = new ArrayList<>();

		this.serverMap.computeIfPresent(world, (worldName, worldMap) -> {
			int blockXMin = CoordinateUtil.chunkToBlock(chunkX);
			Map<Integer, TreeMap<Integer, Map<Integer, T>>> blockXMap = worldMap.subMap(blockXMin, blockXMin + 16);

			blockXMap.entrySet().removeIf(blockXMapping -> {
				int blockZMin = CoordinateUtil.chunkToBlock(chunkZ);
				Map<Integer, Map<Integer, T>> blockZMap = blockXMapping.getValue().subMap(blockZMin, blockZMin + 16);

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

			worldMap.forEach((x, xMap) -> {
				xMap.forEach((z, zMap) -> {
					zMap.forEach((y, value) -> {
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
					});
				});
			});
		});


		return entries;
	}

}
