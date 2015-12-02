package co.sblock.machines;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import org.reflections.Reflections;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import co.sblock.Sblock;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.Direction;
import co.sblock.module.Module;
import co.sblock.users.Region;

/**
 * A Module for handling Block structures with special functions.
 * 
 * @author Jikoo
 */
public class Machines extends Module {

	/* A Map of Machine names to instances. */
	private final Map<String, Machine> byName;
	/* A Map of Machine key Locations to the corresponding Block Locations. */
	private final Multimap<Location, Location> machineBlocks;
	/* A Map of all exploded blocks. */
	private final Map<Block, Boolean> exploded;
	/* The MachineInventoryTracker. */
	private final MachineInventoryTracker tracker;
	/* The YamlConfiguration containing all stored data. */
	private YamlConfiguration storage;

	public Machines(Sblock plugin) {
		super(plugin);
		byName = new HashMap<>();
		this.machineBlocks = HashMultimap.create();
		this.exploded = new HashMap<>();
		this.tracker = new MachineInventoryTracker(this);
		Reflections reflections = new Reflections("co.sblock.machines.type");
		for (Class<? extends Machine> type : reflections.getSubTypesOf(Machine.class)) {
			if (Modifier.isAbstract(type.getModifiers())) {
				continue;
			}
			Machine machine;
			try {
				Constructor<? extends Machine> constructor = type.getConstructor(Sblock.class, this.getClass());
				machine = constructor.newInstance(getPlugin(), this);
				if (machine.getUniqueDrop() == null) {
					continue;
				}
				byName.put(type.getSimpleName(), machine);
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
				saveAllMachines();
			}
		}.runTaskTimerAsynchronously(getPlugin(), 6000L, 6000L);
		// Saving async should ideally not be a problem - we do not ever load or modify the data elsewhere.
	}

	@Override
	protected void onDisable() {
		this.saveAllMachines();
	}

	/**
	 * Adds a Machine with the given parameters.
	 * 
	 * @param location the Location of the key
	 * @param type the type of the Machine
	 * @param owner the owner of the Machine
	 * @param direction the facing direction
	 * @param data the additional data stored by the Machine
	 * 
	 * @return the Machine created
	 */
	public Pair<Machine, ConfigurationSection> addMachine(Location location, String type,
			UUID owner, Direction direction) {
		if (!byName.containsKey(type)
				|| location.getWorld().getName().equals(Region.DERSE.getWorldName())) {
			return null;
		}
		ConfigurationSection section = storage.createSection(pathFromLoc(location));
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
			machineBlocks.put(key, location);
		}
		return new ImmutablePair<>(type, section);
	}

	public void loadChunkMachines(Chunk chunk) {
		String worldName = chunk.getWorld().getName();
		if (!storage.isSet(worldName) || worldName.equals(Region.DERSE.getWorldName())) {
			// No machines in Derspit.
			return;
		}
		String path = new StringBuilder(chunk.getWorld().getName()).append('.')
				.append(chunk.getX()).append('_').append(chunk.getZ()).toString();
		if (!storage.isConfigurationSection(path)) {
			return;
		}
		ConfigurationSection chunkSection = storage.getConfigurationSection(path);
		Set<String> chunkKeys = chunkSection.getKeys(false);
		for (Iterator<String> iterator = chunkKeys.iterator(); iterator.hasNext();) {
			String xyz = iterator.next();
			String[] split = xyz.split("_");
			try {
				Location key = new Location(chunk.getWorld(), Integer.valueOf(split[0]),
						Integer.valueOf(split[1]), Integer.valueOf(split[2]));
				Pair<Machine, ConfigurationSection> machine = loadMachine(key, chunkSection.getConfigurationSection(xyz));
				if (machine == null) {
					iterator.remove();
				}
			} catch (NumberFormatException e) {
				getLogger().warning("Coordinates cannot be parsed from " + Arrays.toString(split));
			}
		}
		if (chunkKeys.isEmpty()) {
			storage.set(path, null);
		}
	}

	public void unloadChunkMachines(Chunk chunk) {
		String path = new StringBuilder(chunk.getWorld().getName()).append('.')
				.append(chunk.getX()).append('_').append(chunk.getZ()).toString();
		ConfigurationSection chunkSection = storage.getConfigurationSection(path);
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
				}
				machineBlocks.removeAll(key);
			} catch (NumberFormatException e) {
				getLogger().warning("Coordinates cannot be parsed from " + Arrays.toString(split));
			}
		}
		if (chunkKeys.isEmpty()) {
			storage.set(path, null);
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
		storage.set(new StringBuilder(world.getName()).append('.').append(chunkX).append('_')
				.append(chunkZ).toString(), null);
	}

	/**
	 * Loads all Machine data from file.
	 */
	private void loadMachines() {
		File file = new File(getPlugin().getDataFolder(), "machines.yml");
		if (file.exists()) {
			storage = YamlConfiguration.loadConfiguration(file);
		} else {
			storage = new YamlConfiguration();
		}
		for (World world : Bukkit.getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				loadChunkMachines(chunk);
			}
		}
	}

	/**
	 * Saves all Machine data to file.
	 */
	private void saveAllMachines() {
		File file;
		try {
			file = new File(getPlugin().getDataFolder(), "machines.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to create file saving machine data!", e);
		}
		try {
			storage.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save all machines!", e);
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
		return isMachine(block.getLocation());
	}

	/**
	 * Checks a Location to see if there is a Machine there.
	 * 
	 * @param location the Location to check
	 * 
	 * @return true if the Location is a Machine
	 */
	public boolean isMachine(Location location) {
		return machineBlocks.containsValue(location);
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
		if (location == null || !machineBlocks.containsValue(location)) {
			return null;
		}
		Location key = getKeyLocation(location);
		if (key == null) {
			return null;
		}

		ConfigurationSection section = storage.getConfigurationSection(pathFromLoc(key));
		if (section == null) {
			return null;
		}
		return new ImmutablePair<Machine, ConfigurationSection>(byName.get(section.getString("type")), section);
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
			return machineBlocks.get(location);
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
		if (location == null || !machineBlocks.containsValue(location)) {
			return null;
		}
		if (machineBlocks.containsKey(location)) {
			return location;
		}
		for (Entry<Location, Location> entry : machineBlocks.entries()) {
			if (location.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Remove the specified Machine listing.
	 * <p>
	 * Be aware - this does not modify the World. All Blocks will remain.
	 * 
	 * @param location the key Location
	 */
	public void deleteMachine(Location location) {
		if (!machineBlocks.containsKey(location)) {
			return;
		}
		Location key = getKeyLocation(location);
		if (key == null) {
			return;
		}
		String path = pathFromLoc(key);
		ConfigurationSection section = storage.getConfigurationSection(path);
		if (section == null) {
			return;
		}
		machineBlocks.removeAll(key);
		storage.set(path, null);
	}

	/**
	 * Flags block(s) as having been exploded.
	 * 
	 * @param b the Block
	 */
	public void addExplodedBlock(Collection<Block> blocks) {
		for (Block block : blocks) {
			exploded.put(block, true);
		}
	}

	/**
	 * Checks to see if a Machine block is exploded.
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
	 * Register stored blocks as not to be regenerated. For use when a Machine is broken.
	 * 
	 * @param blocks
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
	public String getName() {
		return "Sblock Machines";
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
