package co.sblock.machines;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.effects.PassiveEffect;
import co.sblock.machines.type.Alchemiter;
import co.sblock.machines.type.Computer;
import co.sblock.machines.type.Cruxtruder;
import co.sblock.machines.type.Machine;
import co.sblock.machines.type.PGO;
import co.sblock.machines.type.PunchDesignix;
import co.sblock.machines.type.TotemLathe;
import co.sblock.machines.type.Transportalizer;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.MachineType;
import co.sblock.module.Module;
import co.sblock.users.BukkitSerializer;
import co.sblock.users.User;

/**
 * @author Jikoo
 */
public class SblockMachines extends Module {

	/** The MachineModule instance. */
	private static SblockMachines instance;

	/** A Map of Machine key Locations to corresponding Machine. */
	private Map<Location, Machine> machineKeys;
	/** A Map of Machine Block Locations to the corresponding key Location. */
	private Map<Location, Location> machineBlocks;
	/** A Map of all exploded blocks. */
	private Map<Block, Boolean> exploded;

	/**
	 * @see co.sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		this.machineKeys = new HashMap<>();
		this.machineBlocks = new HashMap<>();
		this.exploded = new HashMap<>();
		this.loadAllMachines();

		// Recipe does nothing on its own - We'll cancel all smelts that do not happen within a TotemLathe.
		Bukkit.addRecipe(new FurnaceRecipe(new ItemStack(Material.NETHER_BRICK_ITEM), Material.NETHER_BRICK_ITEM));
	}

	/**
	 * @see co.sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		this.saveAllMachines();
		instance = null;
	}

	/**
	 * Adds a Machine with the given parameters.
	 * 
	 * @param l the Location of the key
	 * @param m the MachineType
	 * @param owner the owner of the Machine
	 * @param d the facing direction
	 * @param data the additional data stored by the Machine
	 * 
	 * @return the Machine created
	 */
	public Machine addMachine(Location l, MachineType m, String owner, Direction d, String data) {
		Machine machine = null;
		switch (m) {
		case ALCHEMITER:
			machine = new Alchemiter(l, owner, d);
			break;
		case COMPUTER:
			machine = new Computer(l, owner, false);
			break;
		case CRUXTRUDER:
			machine = new Cruxtruder(l, owner);
			break;
		case PERFECTLY_GENERIC_OBJECT:
			machine = new PGO(l, owner);
			break;
		case PUNCH_DESIGNIX:
			machine = new PunchDesignix(l, owner, d);
			break;
		case TOTEM_LATHE:
			machine = new TotemLathe(l, owner, d);
			break;
		case TRANSPORTALIZER:
			machine = new Transportalizer(l, owner, d);
			break;
		default:
			break;
		}
		if (machine == null) {
			return null;
		}
		machineKeys.put(l, machine);
		for (Location l1 : machine.getLocations()) {
			machineBlocks.put(l1, l);
		}
		if (data != null) {
			machine.setData(data);
		}
		return machine;
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
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		for (String machineLocation : yaml.getKeys(false)) {
			addMachine(BukkitSerializer.locationFromString(machineLocation),
					MachineType.getType(yaml.getString(machineLocation + ".type")),
					yaml.getString(machineLocation + ".owner"),
					Direction.valueOf(yaml.getString(machineLocation + ".direction")),
					yaml.getString(machineLocation + ".data"));
		}
	}

	public void saveMachine(Machine machine) {
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "Machines.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load machine data!", e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		String location = machine.getLocationString();
		yaml.set(location + ".type", machine.getType().name());
		yaml.set(location + ".owner", machine.getOwner());
		yaml.set(location + ".direction", machine.getFacingDirection().name());
		yaml.set(location + ".data", machine.getData());
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save machine at " + machine, e);
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
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		for (Machine machine : this.getMachines()) {
			String location = machine.getLocationString();
			yaml.set(location + ".type", machine.getType().name());
			yaml.set(location + ".owner", machine.getOwner());
			yaml.set(location + ".direction", machine.getFacingDirection().name());
			yaml.set(location + ".data", machine.getData());
		}
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save all machines!", e);
		}
	}

	/**
	 * Load a machine from the database.
	 * <p>
	 * This method has no error handling. Don't screw up.
	 * 
	 * @param location the Location String
	 * @param machineType the MachineType String
	 * @param owner the owner of the Machine
	 * @param direction the facing direction
	 */
	public void loadMachine(String location, String machineType, String owner, byte direction, String data) { // TODO remove once yaml
		String[] l = location.split(",");
		addMachine(new Location(Bukkit.getWorld(l[0]), Integer.parseInt(l[1]),
				Integer.parseInt(l[2]), Integer.parseInt(l[3])),
				MachineType.getType(machineType), owner, Direction.getDirection(direction), data);
		
	}

	/**
	 * Checks a Location to see if there is a Machine there.
	 * 
	 * @param l the Location to check
	 * 
	 * @return true if the Location is a Machine
	 */
	public boolean isMachine(Location l) {
		return machineKeys.containsKey(l) || machineBlocks.containsKey(l);
	}

	/**
	 * Checks a Block to see if it is part of a Machine.
	 * 
	 * @param b the Block to check
	 * 
	 * @return true if the Block is a Machine
	 */
	public boolean isMachine(Block b) {
		return machineKeys.containsKey(b.getLocation()) || machineBlocks.containsKey(b.getLocation());
	}

	/**
	 * Gets a Machine from a Block.
	 * 
	 * @param b the Block
	 * 
	 * @return the Machine
	 */
	public Machine getMachineByBlock(Block b) {
		Machine m = machineKeys.get(b.getLocation());
		if (m == null) {
			m = machineKeys.get(machineBlocks.get(b.getLocation()));
		}
		return m;
	}

	/**
	 * Gets a Machine from a Location.
	 * 
	 * @param l the Location
	 * 
	 * @return the Machine
	 */
	public Machine getMachineByLocation(Location l) {
		Machine m = machineKeys.get(l);
		if (m == null) {
			m = machineKeys.get(machineBlocks.get(l));
		}
		return m;
	}

	/**
	 * Gets a Set of all Machine key Locations.
	 * 
	 * @return the Set
	 */
	public Set<Location> getMachineLocs() {
		return machineKeys.keySet();
	}

	public Collection<Machine> getMachines() {
		return machineKeys.values();
	}

	/**
	 * Gets a Set of all Machine key Locations by MachineType.
	 * 
	 * @param m the MachineType
	 * 
	 * @return the Set
	 */
	public Set<Location> getMachines(MachineType m) {
		Set<Location> set = new HashSet<Location>();
		for (Entry<Location, Machine> e : machineKeys.entrySet()) {
			if (e.getValue().getType().equals(m)) {
				set.add(e.getKey());
			}
		}
		return set;
	}

	/**
	 * Remove the specified Machine listing.
	 * <p>
	 * Be aware - this does not modify the World. All Blocks will remain.
	 * 
	 * @param l the key Location
	 */
	public void deleteMachine(Location l) {
		if (!machineKeys.containsKey(l)) {
			return;
		}
		for (Iterator<Entry<Location, Location>> iterator = machineBlocks.entrySet().iterator(); iterator.hasNext();) {
			Entry<Location, Location> entry = iterator.next();
			if (entry.getValue().equals(l)) {
				iterator.remove();
				setRemoved(entry.getKey().getBlock());
			}
		}
		Machine machine = machineKeys.remove(l);
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "Machines.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load machine data!", e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		yaml.set(machine.getLocationString(), null);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to delete machine at " + machine.getLocationString(), e);
		}
	}

	/**
	 * Flags block(s) as having been exploded.
	 * 
	 * @param b the Block
	 */
	public void addBlock(Block... blocks) {
		for (Block b : blocks) {
			exploded.put(b, true);
		}
	}

	/**
	 * Checks to see if a Machine block is exploded.
	 * 
	 * @param b the Block to check
	 * 
	 * @return true if the block is recorded as being exploded.
	 */
	public boolean isExploded(Block b) {
		return exploded.containsKey(b);
	}

	/**
	 * Marks a Block as having been restored post-explosion.
	 * 
	 * @param b the Block
	 */
	public void setRestored(Block b) {
		exploded.remove(b);
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
	 * @param b the Block
	 * 
	 * @return true if the block is to be restored
	 */
	public boolean shouldRestore(Block b) {
		if (exploded.containsKey(b)) {
			return exploded.get(b);
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
	 * @param p the Player
	 * @param key the location of the Computer just assembled
	 * 
	 * @return true if the Player has placed a Computer
	 */
	public boolean hasComputer(Player p, Location key) {
		for (Machine m : machineKeys.values()) {
			if (m instanceof Computer && m.getOwner().equals(p.getUniqueId().toString()) && !m.getKey().equals(key)) {
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
	 * @return the matching Machine, or null if the Player has no computer.
	 */
	public Machine getComputer(UUID playerID) {
		for (Machine m : machineKeys.values()) {
			if (m instanceof Computer && m.getOwner().equals(playerID.toString())) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Check to see if the Player is within range of a Computer.
	 * 
	 * @return true if the Player is within 10 meters of a Computer.
	 */
	public static boolean hasComputerAccess(User user) {
		if (user.getPassiveEffects().containsKey(PassiveEffect.COMPUTER)) {
			return true;
		}
		return getInstance().isByComputer(user.getPlayer(), 10);
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
		for (Machine m : this.getMachinesInProximity(p.getLocation(), distance, MachineType.COMPUTER, true)) {
			if (m.getOwner().equals(p.getUniqueId().toString())) {
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
	public Set<Machine> getMachinesInProximity(Location current, int searchDistance,
			MachineType mt, boolean keyRequired) {
		Set<Machine> machines = new HashSet<Machine>();
		// distance^2 once > blocks to check * root(distance from current)
		searchDistance = (int) Math.pow(searchDistance, 2);
		if (!keyRequired) {
			for (Entry<Location, Location> e : machineBlocks.entrySet()) {
				if (e.getKey().getWorld().equals(current.getWorld())
						&& current.distanceSquared(e.getKey()) <= searchDistance) {
					Machine m = this.getMachineByLocation(e.getValue());
					if (mt == m.getType() || mt == MachineType.ANY) {
						machines.add(m);
					}
				}
			}
		}
		for (Entry<Location, Machine> e : machineKeys.entrySet()) {
			if (e.getKey().getWorld().equals(current.getWorld())
					&& current.distanceSquared(e.getKey()) <= searchDistance) {
				if (mt == e.getValue().getType() || mt == MachineType.ANY) {
					machines.add(e.getValue());
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
	public Set<Machine> getMachines(UUID playerID) {
		HashSet<Machine> machines = new HashSet<>();
		for (Machine m : machineKeys.values()) {
			if (m.getOwner().equals(playerID.toString())) {
				machines.add(m);
			}
		}
		return machines;
	}

	@Override
	protected String getModuleName() {
		return "Machines";
	}

	/**
	 * Gets the current instance of MachineModule.
	 * 
	 * @return the MachineModule
	 */
	public static SblockMachines getInstance() {
		return instance;
	}
}
