package co.sblock.machines;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import org.reflections.Reflections;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import co.sblock.Sblock;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.Direction;
import co.sblock.module.Module;

/**
 * @author Jikoo
 */
public class Machines extends Module {

	private static Machines instance;

	/* A Map of Machine names to instances. */
	private Map<String, Machine> byName;
	/* A Map of Machine key Locations to the corresponding Block Locations. */
	private Multimap<Location, Location> machineBlocks;
	/* A Map of all exploded blocks. */
	private Map<Block, Boolean> exploded;
	/* The YamlConfiguration containing all stored data. */
	private YamlConfiguration storage;

	@Override
	protected void onEnable() {
		instance = this;
		this.byName = new HashMap<>();
		Reflections reflections = new Reflections("co.sblock.machines.type");
		for (Class<? extends Machine> type : reflections.getSubTypesOf(Machine.class)) {
			if (Modifier.isAbstract(type.getModifiers())) {
				continue;
			}
			Machine machine;
			try {
				machine = type.newInstance();
				byName.put(type.getSimpleName(), machine);
			} catch (InstantiationException | IllegalAccessException e) {
				// Improperly set up Machine
				e.printStackTrace();
			}
		}
		this.machineBlocks = HashMultimap.create();
		this.exploded = new HashMap<>();
		this.loadAllMachines();

		new BukkitRunnable() {
			@Override
			public void run() {
				saveAllMachines();
			}
		}.runTaskTimerAsynchronously(Sblock.getInstance(), 6000L, 6000L);
		// Saving async should ideally not be a problem - we do not ever load or modify the data elsewhere.
	}

	@Override
	protected void onDisable() {
		this.saveAllMachines();
		instance = null;
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
	public Pair<Machine, ConfigurationSection> addMachine(Location location, String type, UUID owner, Direction direction) {
		if (!byName.containsKey(type)) {
			return null;
		}
		ConfigurationSection section = storage.createSection(fromLocation(location));
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
	public Pair<Machine, ConfigurationSection> loadMachine(Location key, ConfigurationSection section) {
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

	public void loadAllMachines() {
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "Machines.yml");
			if (!file.exists()) {
				file.createNewFile();
				return;
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load machine data!", e);
		}
		storage = YamlConfiguration.loadConfiguration(file);
		for (String machineLocation : storage.getKeys(false)) {
			loadMachine(fromString(machineLocation), storage.getConfigurationSection(machineLocation));
		}
	}

	public void saveAllMachines() {
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "Machines.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load machine data!", e);
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

		ConfigurationSection section = storage.getConfigurationSection(fromLocation(key));
		if (section == null) {
			return null;
		}
		return new ImmutablePair<Machine, ConfigurationSection>(byName.get(storage.getString("type")), section);
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
		String path = fromLocation(key);
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
	public void addExplodedBlock(Block... blocks) {
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
	 * Check to see if the Player in question has placed a Computer.
	 * <p>
	 * For use in assembling a new Computer - Players are only allowed one.
	 * 
	 * @see co.sblock.Machines.Type.Computer#assemble(org.bukkit.event.block.BlockPlaceEvent)
	 * 
	 * @param player the Player
	 * @param key the location of the Computer just assembled
	 * 
	 * @return true if the Player has placed a Computer
	 */
	public boolean hasComputer(Player player, Location key) {
		String keyPath = fromLocation(key);
		for (String path : storage.getKeys(false)) {
			if (path.equals(keyPath) || !storage.getString(path + ".type").equals("Computer")) {
				continue;
			}
			if (storage.getString(path + ".owner").equals(player.getUniqueId().toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the Computer owned by a particular Player.
	 * 
	 * @param playerID the UUID of the Player
	 * 
	 * @return the matching Machine and ConfigurationSection, or null if the Player has no computer.
	 */
	public Pair<Machine, ConfigurationSection> getComputer(UUID playerID) {
		for (String path : storage.getKeys(false)) {
			if (!"Computer".equals(storage.getString(path + ".type"))) {
				continue;
			}
			if (playerID.toString().equals(storage.getString(path + ".owner"))) {
				return new ImmutablePair<Machine, ConfigurationSection>(byName.get("Computer"), storage);
			}
		}
		return null;
	}

	/**
	 * Check if a Player is within the specified radius of their Computer.
	 * 
	 * @param p the Player
	 * @param distance the radius to search
	 * 
	 * @return true if the Player is within the radius
	 */
	public boolean isByComputer(Player p, int distance) {
		for (ConfigurationSection data : this.getMachinesInProximity(p.getLocation(), distance, "Computer", true)) {
			if (data.getString("owner").equals(p.getUniqueId().toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a Set of Machines within a specified radius. If no Machines match the specified
	 * conditions, an empty Set is returned.
	 * 
	 * @param current the current Location (presumably of a Player)
	 * @param searchDistance the radius from current that is acceptable
	 * @param keyRequired true if the key Location must be within the radius. Less intensive search.
	 * @param mt the MachineType to search for
	 * 
	 * @return all machines of the correct type within the specified radius
	 */
	public Set<ConfigurationSection> getMachinesInProximity(Location current, int searchDistance,
			String type, boolean keyRequired) {
		Set<ConfigurationSection> machines = new HashSet<>();
		if (type != null) {
			if (!byName.containsKey(type)) {
				return machines;
			}
		}
		// distance^2 once > blocks to check * root(distance from current)
		searchDistance = (int) Math.pow(searchDistance, 2);
		for (Location location : machineBlocks.keySet()) {
			if (location.getWorld().equals(current.getWorld())
					&& current.distanceSquared(location) <= searchDistance) {
				Pair<Machine, ConfigurationSection> pair = this.getMachineByLocation(location);
				if (type == null || type.equals(pair.getRight().getString("type"))) {
					machines.add(pair.getRight());
				}
			}
		}
		return machines;
	}

	/**
	 * Gets all machines with the Player's UUID for data.
	 * 
	 * @param playerID the Player's UUID
	 * 
	 * @return the Set of Machines
	 */
	public Set<Pair<Machine, ConfigurationSection>> getMachines(UUID playerID) {
		HashSet<Pair<Machine, ConfigurationSection>> machines = new HashSet<>();
		for (String path : storage.getKeys(false)) {
			if (!"Computer".equals(storage.getString(path + ".type"))) {
				continue;
			}
			if (playerID.toString().equals(storage.getString(path + ".owner"))) {
				machines.add(new ImmutablePair<Machine, ConfigurationSection>(byName.get("Computer"), storage));
			}
		}
		return machines;
	}

	/**
	 * Gets a Map of instances of Machines stored by name.
	 * 
	 * @return the Map of all Machine instances stored by name
	 */
	public static Map<String, Machine> getMachinesByName() {
		return getInstance().byName;
	}

	/**
	 * Gets an instance of a Machine by name.
	 * 
	 * @param name the name of the Machine
	 * @return the Machine instance
	 */
	public static Machine getMachineByName(String name) {
		return getInstance().byName.get(name);
	}

	/**
	 * Gets the current instance of MachineModule.
	 * 
	 * @return the MachineModule
	 */
	public static Machines getInstance() {
		return instance;
	}

	@Override
	protected String getModuleName() {
		return "Sblock Machines";
	}

	public static String fromLocation(Location location) {
		return new StringBuilder(location.getWorld().getName()).append('_')
				.append(location.getBlockX()).append('_').append(location.getBlockY()).append('_')
				.append(location.getBlockZ()).toString();
	}

	public static Location fromString(String string) {
		String[] split = string.split("_");
		return new Location(Bukkit.getWorld(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
	}
}
