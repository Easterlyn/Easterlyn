package com.easterlyn.machines;

import com.easterlyn.Easterlyn;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.module.Module;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A Module for handling Block structures with special functions.
 * 
 * @author Jikoo
 */
public class Machines extends Module {

	/* A Map of Machine names to instances. */
	private final Map<String, Machine> byName;
	/* A Map of Machine Block Locations to keys */
	private final Map<Integer, Map<Integer, Map<Integer, Map<String, Location>>>> blocksToKeys;
	/* A Map of Machine key Locations to the corresponding Block Locations. */
	private final Multimap<Location, Location> keysToBlocks;
	/* A Map of all exploded blocks. */
	private final Map<Block, Boolean> exploded;
	/* The MachineInventoryTracker. */
	private final MachineInventoryTracker tracker;

	public Machines(Easterlyn plugin) {
		super(plugin);
		byName = new HashMap<>();
		this.blocksToKeys = new HashMap<>();
		this.keysToBlocks = HashMultimap.create();
		this.exploded = new HashMap<>();
		this.tracker = new MachineInventoryTracker(this);
		Reflections reflections = new Reflections("com.easterlyn.machines.type");
		for (Class<? extends Machine> type : reflections.getSubTypesOf(Machine.class)) {
			if (Modifier.isAbstract(type.getModifiers())) {
				continue;
			}
			Machine machine;
			try {
				Constructor<? extends Machine> constructor = type.getConstructor(Easterlyn.class, this.getClass());
				machine = constructor.newInstance(getPlugin(), this);
				if (machine.getUniqueDrop() == null) {
					continue;
				}
				byName.put(type.getSimpleName(), machine);
				byName.put(machine.getName(), machine);
				ItemStack item = machine.getUniqueDrop();
				if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
					continue;
				}
				ItemMeta meta = item.getItemMeta();
				if (meta.hasDisplayName()) {
					byName.put(meta.getDisplayName(), machine);
				}
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
					| SecurityException | IllegalArgumentException | InvocationTargetException e) {
				// Improperly set up Machine
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onEnable() {
		this.loadMachines();

		new BukkitRunnable() {
			@Override
			public void run() {
				saveConfig();
			}
		}.runTaskTimerAsynchronously(getPlugin(), 6000L, 6000L);
		// Saving async should ideally not be a problem - we do not ever load or modify the data elsewhere.
	}

	@Override
	protected void onDisable() { }

	/**
	 * Adds a Machine with the given parameters.
	 * 
	 * @param location the Location of the key
	 * @param type the type of the Machine
	 * @param owner the owner of the Machine
	 * @param direction the facing direction
	 * 
	 * @return the Machine created
	 */
	public Pair<Machine, ConfigurationSection> addMachine(Location location, String type,
			UUID owner, Direction direction) {
		if (!this.isEnabled() || !byName.containsKey(type) || !this.getPlugin().getConfig()
				.getStringList("machines.worlds").contains(location.getWorld().getName())) {
			return null;
		}
		ConfigurationSection section = getConfig().createSection(pathFromLoc(location));
		section.set("type", type);
		section.set("owner", owner.toString());
		section.set("direction", direction.name());
		return loadMachine(location, section);
	}

	/**
	 * Loads a Machine from a ConfigurationSection.
	 * 
	 * @param key the key Location of the Machine
	 * @param section the ConfigurationSection
	 * @return the Machine type loaded
	 */
	private Pair<Machine, ConfigurationSection> loadMachine(Location key, ConfigurationSection section) {
		if (!byName.containsKey(section.getString("type"))) {
			return null;
		}
		Machine type = byName.get(section.getString("type"));
		Direction direction = type.getDirection(section);
		for (Location location : type.getShape().getBuildLocations(key, direction).keySet()) {
			this.addMachineBlock(location, key);
		}
		return new ImmutablePair<>(type, section);
	}

	public void enableChunkMachines(Chunk chunk) {
		if (!this.isEnabled()) {
			return;
		}
		String worldName = chunk.getWorld().getName();
		if (!getConfig().isSet(worldName) || !this.getPlugin().getConfig()
				.getStringList("machines.worlds").contains(worldName)) {
			// Disabled world.
			return;
		}
		String path = new StringBuilder(chunk.getWorld().getName()).append('.')
				.append(chunk.getX()).append('_').append(chunk.getZ()).toString();
		if (!getConfig().isConfigurationSection(path)) {
			return;
		}
		ConfigurationSection chunkSection = getConfig().getConfigurationSection(path);
		Set<String> chunkKeys = chunkSection.getKeys(false);
		for (Iterator<String> iterator = chunkKeys.iterator(); iterator.hasNext();) {
			String xyz = iterator.next();
			String[] split = xyz.split("_");
			try {
				Location key = new Location(chunk.getWorld(), Integer.valueOf(split[0]),
						Integer.valueOf(split[1]), Integer.valueOf(split[2]));
				Pair<Machine, ConfigurationSection> machine = getMachineByLocation(key);
				if (machine == null) {
					iterator.remove();
					continue;
				}
				machine.getLeft().enable(machine.getRight());
			} catch (NumberFormatException e) {
				getLogger().warning("Coordinates cannot be parsed from " + Arrays.toString(split));
			}
		}
		if (chunkKeys.isEmpty()) {
			getConfig().set(path, null);
		}
	}

	public void disableChunkMachines(Chunk chunk) {
		if (!this.isEnabled()) {
			return;
		}
		String path = new StringBuilder(chunk.getWorld().getName()).append('.')
				.append(chunk.getX()).append('_').append(chunk.getZ()).toString();
		ConfigurationSection chunkSection = getConfig().getConfigurationSection(path);
		if (chunkSection == null) {
			return;
		}
		Set<String> chunkKeys = chunkSection.getKeys(false);
		for (Iterator<String> iterator = chunkKeys.iterator(); iterator.hasNext();) {
			String xyz = iterator.next();
			String[] split = xyz.split("_");
			try {
				Location key = new Location(chunk.getWorld(), Integer.valueOf(split[0]),
						Integer.valueOf(split[1]), Integer.valueOf(split[2]));
				Pair<Machine, ConfigurationSection> machine = getMachineByLocation(key);
				if (machine == null) {
					iterator.remove();
					continue;
				}
				machine.getLeft().disable(machine.getRight());
			} catch (NumberFormatException e) {
				getLogger().warning("Coordinates cannot be parsed from " + Arrays.toString(split));
			}
		}
		if (chunkKeys.isEmpty()) {
			getConfig().set(path, null);
		}
	}

	/**
	 * Delete all Machines stored in a Chunk. Does not unload them if loaded.
	 * <p>
	 * This method is purely for use in the RegioneratorChunkDeleteListener, chunks (and, therefore,
	 * the machines in them) should not be loaded.
	 * 
	 * @param world the World
	 * @param chunkX the Chunk X
	 * @param chunkZ the Chunk Z
	 */
	public void deleteChunkMachines(World world, int chunkX, int chunkZ) {
		getConfig().set(new StringBuilder(world.getName()).append('.').append(chunkX).append('_')
				.append(chunkZ).toString(), null);
	}

	/**
	 * Loads all Machine data from file.
	 */
	private void loadMachines() {
		for (World world : Bukkit.getWorlds()) {
			String worldName = world.getName();
			if (!getConfig().isConfigurationSection(worldName) || !this.getPlugin().getConfig()
					.getStringList("machines.worlds").contains(worldName)) {
				// Disabled world.
				continue;
			}
			ConfigurationSection worldSection = getConfig().getConfigurationSection(worldName);
			for (String chunkPath : worldSection.getKeys(false)) {
				if (!worldSection.isConfigurationSection(chunkPath)) {
					continue;
				}

				boolean chunkLoaded;
				try {
					String[] chunkSplit = chunkPath.split("_");
					chunkLoaded = world.isChunkLoaded(Integer.valueOf(chunkSplit[0]), Integer.valueOf(chunkSplit[1]));
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
					chunkLoaded = false;
				}

				ConfigurationSection chunkSection = worldSection.getConfigurationSection(chunkPath);
				Set<String> chunkKeys = chunkSection.getKeys(false);
				for (Iterator<String> iterator = chunkKeys.iterator(); iterator.hasNext();) {
					String xyz = iterator.next();
					String[] split = xyz.split("_");
					try {
						Location key = new Location(world, Integer.valueOf(split[0]),
								Integer.valueOf(split[1]), Integer.valueOf(split[2]));
						Pair<Machine, ConfigurationSection> machine = loadMachine(key, chunkSection.getConfigurationSection(xyz));
						if (machine == null) {
							iterator.remove();
							System.out.println("Deleting broken machine at " + key);
							continue;
						}
						if (chunkLoaded) {
							machine.getLeft().enable(machine.getRight());
						}
					} catch (NumberFormatException e) {
						getLogger().warning("Coordinates cannot be parsed from " + Arrays.toString(split));
					}
				}
			}
		}
	}

	private void addMachineBlock(Location block, Location key) {
		this.keysToBlocks.put(key, block);
		int x = (int) block.getX();
		int y = (int) block.getY();
		int z = (int) block.getZ();
		Map<Integer, Map<Integer, Map<String, Location>>> yzworld;
		if (this.blocksToKeys.containsKey(x)) {
			yzworld = this.blocksToKeys.get(x);
		} else {
			yzworld = new HashMap<>();
			this.blocksToKeys.put(x, yzworld);
		}
		Map<Integer, Map<String, Location>> zworld;
		if (yzworld.containsKey(y)) {
			zworld = yzworld.get(y);
		} else {
			zworld = new HashMap<>();
			yzworld.put(y, zworld);
		}
		Map<String, Location> world;
		if (zworld.containsKey(z)) {
			world = zworld.get(z);
		} else {
			world = new HashMap<>();
			zworld.put(z, world);
		}
		world.put(block.getWorld().getName(), key);
	}

	private void removeMachineBlocks(Location key) {
		if (!this.keysToBlocks.containsKey(key)) {
			return;
		}
		String worldName = key.getWorld().getName();
		for (Location block : this.keysToBlocks.removeAll(key)) {

			int x = (int) block.getX();
			if (!this.blocksToKeys.containsKey(x)) {
				continue;
			}

			int y = (int) block.getY();
			Map<Integer, Map<Integer, Map<String, Location>>> yzworld = this.blocksToKeys.get(x);
			if (!yzworld.containsKey(y)) {
				continue;
			}

			int z = (int) block.getZ();
			Map<Integer, Map<String, Location>> zworld = yzworld.get(y);
			if (!zworld.containsKey(z)) {
				continue;
			}

			Map<String, Location> world = zworld.get(z);
			if (!world.containsKey(worldName)) {
				continue;
			}

			world.remove(worldName);

			// Clean up empty maps
			if (!world.isEmpty()) {
				continue;
			}
			zworld.remove(z);
			if (!zworld.isEmpty()) {
				continue;
			}
			yzworld.remove(y);
			if (!yzworld.isEmpty()) {
				continue;
			}
			this.blocksToKeys.remove(x);
		}
	}

	/**
	 * Checks a Block to see if it is part of a Machine.
	 * 
	 * @param block the Block to check
	 * 
	 * @return true if the Block is a Machine
	 */
	public boolean isMachine(Block block) {
		return isMachine(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	}

	/**
	 * Checks a Location to see if there is a Machine there.
	 * 
	 * @param location the Location to check
	 * 
	 * @return true if the Location is a Machine
	 */
	public boolean isMachine(Location location) {
		return isMachine((int) location.getX(), (int) location.getY(), (int) location.getZ(),
				location.getWorld().getName());
	}

	/**
	 * Checks coordinates to see if a Machine is present.
	 * 
	 * @param x the Block X coordinate
	 * @param y the Block Y coordinate
	 * @param z the Block Z coordinate
	 * @param worldName the name of the World
	 * 
	 * @return true if the coordinates are part of a Machine
	 */
	private boolean isMachine(int x, int y, int z, String worldName) {
		if (!this.blocksToKeys.containsKey(x)) {
			return false;
		}
		Map<Integer, Map<Integer, Map<String, Location>>> yzworld = this.blocksToKeys.get(x);
		if (!yzworld.containsKey(y)) {
			return false;
		}
		Map<Integer, Map<String, Location>> zworld = yzworld.get(y);
		return zworld.containsKey(z) && zworld.get(z).containsKey(worldName);
	}

	/**
	 * Gets a Machine from a Block.
	 * 
	 * @param block the Block
	 * 
	 * @return the Machine
	 */
	public Pair<Machine, ConfigurationSection> getMachineByBlock(Block block) {
		return getMachineByLocation(block.getLocation());
	}

	/**
	 * Gets a Machine from a Location.
	 * 
	 * @param location the Location
	 * 
	 * @return the Machine
	 */
	public Pair<Machine, ConfigurationSection> getMachineByLocation(Location location) {
		if (location == null) {
			return null;
		}
		Location key = getKeyLocation(location);
		if (key == null) {
			return null;
		}

		ConfigurationSection section = getConfig().getConfigurationSection(pathFromLoc(key));
		if (section == null) {
			return null;
		}
		return new ImmutablePair<>(byName.get(section.getString("type")), section);
	}

	/**
	 * Gets all locations defined as part of the Machine with the given key Location.
	 * 
	 * @param location the Location of the Machine
	 * @return a Collection of all Locations in the Machine, or null if the Location is not a Machine
	 */
	public Collection<Location> getMachineBlocks(Location location) {
		location = getKeyLocation(location);
		if (location != null) {
			return keysToBlocks.get(location);
		}
		return null;
	}

	/**
	 * Gets the key location of a Machine from any location within it.
	 * 
	 * @param location the location.
	 * @return the key location, or null if the location is not a Machine.
	 */
	private Location getKeyLocation(Location location) {
		if (location == null) {
			return null;
		}
		if (this.keysToBlocks.containsKey(location)) {
			return location;
		}

		int x = (int) location.getX();
		if (!this.blocksToKeys.containsKey(x)) {
			return null;
		}

		int y = (int) location.getY();
		Map<Integer, Map<Integer, Map<String, Location>>> yzworld = this.blocksToKeys.get(x);
		if (!yzworld.containsKey(y)) {
			return null;
		}

		int z = (int) location.getZ();
		Map<Integer, Map<String, Location>> zworld = yzworld.get(y);
		if (!zworld.containsKey(z)) {
			return null;
		}

		Map<String, Location> world = zworld.get(z);
		String worldName = location.getWorld().getName();
		if (!world.containsKey(worldName)) {
			return null;
		}
		return world.get(worldName);
	}

	/**
	 * Remove the specified Machine listing.
	 * <p>
	 * Be aware - this does not modify the World. All Blocks will remain.
	 * 
	 * @param location the key Location
	 */
	public void deleteMachine(Location location) {
		Location key = getKeyLocation(location);
		if (key == null) {
			return;
		}
		this.removeMachineBlocks(key);
		String path = pathFromLoc(key);
		ConfigurationSection section = getConfig().getConfigurationSection(path);
		if (section == null) {
			return;
		}
		getConfig().set(path, null);
	}

	/**
	 * Flags Block(s) as having been exploded.
	 * 
	 * @param blocks the Blocks
	 */
	public void addExplodedBlock(Collection<Block> blocks) {
		for (Block block : blocks) {
			exploded.put(block, true);
		}
	}

	/**
	 * Checks to see if a Machine Block is exploded.
	 * 
	 * @param block the Block to check
	 * 
	 * @return true if the block is recorded as being exploded.
	 */
	public boolean isExploded(Block block) {
		return exploded.containsKey(block);
	}

	/**
	 * Marks a Block as having been restored post-explosion.
	 * 
	 * @param block the Block
	 */
	public void setRestored(Block block) {
		exploded.remove(block);
	}

	/**
	 * Register stored Blocks as not to be regenerated. For use when a Machine is broken.
	 * 
	 * @param blocks the Blocks
	 */
	public void setRemoved(Block... blocks) {
		for (Block b : blocks) {
			if (exploded.containsKey(b)) {
				exploded.put(b, false);
			}
		}
	}

	/**
	 * Checks if a Block should be replaced post-explosion. This allows Machines to be unregistered
	 * while partially exploded.
	 * 
	 * @param block the Block
	 * 
	 * @return true if the block is to be restored
	 */
	public boolean shouldRestore(Block block) {
		if (exploded.containsKey(block)) {
			return exploded.get(block);
		}
		return true;
	}

	/**
	 * Gets a Map of instances of Machines stored by name.
	 * 
	 * @return the Map of all Machine instances stored by name
	 */
	public Map<String, Machine> getMachinesByName() {
		return byName;
	}

	/**
	 * Gets an instance of a Machine by name.
	 * 
	 * @param name the name of the Machine
	 * @return the Machine instance
	 */
	public Machine getMachineByName(String name) {
		return byName.get(name);
	}

	/**
	 * Gets the MachineInventoryTracker used to open and link special inventories for Machines.
	 * 
	 * @return the MachineInventoryTracker
	 */
	public MachineInventoryTracker getInventoryTracker() {
		return this.tracker;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "Machines";
	}

	public static String pathFromLoc(Location location) {
		return new StringBuilder(location.getWorld().getName()).append('.')
				.append(location.getBlockX() >> 4).append('_').append(location.getBlockZ() >> 4)
				.append('.').append(location.getBlockX()).append('_').append(location.getBlockY())
				.append('_').append(location.getBlockZ()).toString();
	}

	public static Location locFromPath(String string) {
		String[] pathSplit = string.split("\\.");
		String[] xyz = pathSplit[2].split("_");
		return new Location(Bukkit.getWorld(pathSplit[0]), Integer.valueOf(xyz[0]), Integer.valueOf(xyz[1]), Integer.valueOf(xyz[2]));
	}

}
