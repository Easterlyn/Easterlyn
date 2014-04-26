package co.sblock.machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import co.sblock.data.SblockData;
import co.sblock.effects.PassiveEffect;
import co.sblock.machines.type.Alchemiter;
import co.sblock.machines.type.Bank;
import co.sblock.machines.type.Computer;
import co.sblock.machines.type.Cruxtruder;
import co.sblock.machines.type.Direction;
import co.sblock.machines.type.Machine;
import co.sblock.machines.type.MachineType;
import co.sblock.machines.type.PGO;
import co.sblock.machines.type.PunchDesignix;
import co.sblock.machines.type.TotemLathe;
import co.sblock.machines.type.Transmaterializer;
import co.sblock.machines.type.Transportalizer;
import co.sblock.users.User;

/**
 * @author Jikoo
 */
public class MachineManager {

	/** A Map of Machine key Locations to corresponding Machine. */
	private Map<Location, Machine> machineKeys;
	/** A Map of Machine Block Locations to the corresponding key Location. */
	private Map<Location, Location> machineBlocks;
	/** A Map of all exploded blocks. */
	private Set<Block> exploded;

	public MachineManager() {
		this.machineKeys = new HashMap<Location, Machine>();
		this.machineBlocks = new HashMap<Location, Location>();
		this.exploded = new HashSet<Block>();
	}

	/**
	 * Adds a Machine with the given parameters.
	 * 
	 * @param l the Location of the key
	 * @param m the MachineType
	 * @param data the additional Machine data
	 * @param d the facing direction
	 * 
	 * @return the Machine created
	 */
	public Machine addMachine(Location l, MachineType m, String data, Direction d) {
		Machine machine = null;
		switch (m) {
		case ALCHEMITER:
			machine = new Alchemiter(l, data, d);
			break;
		case BANK:
			machine = new Bank(l, data);
		case COMPUTER:
			machine = new Computer(l, data, false);
			break;
		case CRUXTRUDER:
			machine = new Cruxtruder(l, data);
			break;
		case PERFECTLY_GENERIC_OBJECT:
			machine = new PGO(l, data);
			break;
		case PUNCH_DESIGNIX:
			machine = new PunchDesignix(l, data, d);
			break;
		case TOTEM_LATHE:
			machine = new TotemLathe(l, data, d);
			break;
		case TRANSMATERIALIZER:
			machine = new Transmaterializer(l, data, d);
			break;
		case TRANSPORTALIZER:
			machine = new Transportalizer(l, data, d);
			break;
		default:
			break;
		}
		if (machine == null) {
			return null;
		}
		machineKeys.put(l, machine);
		for (Location l1 : machine.getLocations()) {
			if (!machineBlocks.containsKey(l1)) {
				machineBlocks.put(l1, l);
			}
		}
		return machine;
	}

	/**
	 * Load a machine from the database.
	 * <p>
	 * This method has no error handling. Don't screw up.
	 * 
	 * @param location the Location String
	 * @param machineType the MachineType String
	 * @param data the additional Machine data
	 * @param direction the facing direction
	 */
	public void loadMachine(String location, String machineType, String data, byte direction) {
		String[] l = location.split(",");
		addMachine(new Location(Bukkit.getWorld(l[0]), Integer.parseInt(l[1]),
				Integer.parseInt(l[2]), Integer.parseInt(l[3])),
				MachineType.getType(machineType), data, Direction.getDirection(direction));
		
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
	public Set<Location> getMachines() {
		return machineKeys.keySet();
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
	public void removeMachineListing(Location l) {
		if (machineKeys.containsKey(l)) {
			SblockData.getDB().deleteMachine(machineKeys.remove(l));
			ArrayList<Location> stagedRemoval = new ArrayList<Location>();
			for (Entry<Location, Location> e : machineBlocks.entrySet()) {
				if (e.getValue().equals(l)) {
					stagedRemoval.add(e.getKey());
				}
			}
			for (Location l1 : stagedRemoval) {
				machineBlocks.remove(l1);
			}
		}
	}

	/**
	 * Save all stored Machines to the database.
	 */
	public void saveToDb() {
		for (Location l : machineKeys.keySet()) {
			SblockData.getDB().saveMachine(machineKeys.get(l));
		}
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
			if (m.getData().equals(p.getUniqueId().toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a Set of Machines within a specified radius. If no Machines match
	 * the specified conditions, an empty Set is returned.
	 * 
	 * @param current the current Location (presumably of a Player)
	 * @param searchDistance the radius from current that is acceptable
	 * @param keyRequired true if the key Location must be within the radius.
	 *        Less intensive search.
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
			if (m instanceof Computer && m.getData().equals(p.getUniqueId().toString()) && !m.getKey().equals(key)) {
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
			if (m instanceof Computer && m.getData().equals(playerID.toString())) {
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
		return SblockMachines.getMachines().getManager().isByComputer(user.getPlayer(), 10);
	}

	/**
	 * Checks to see if a Machine block is exploded.
	 * 
	 * @param b the Block to check
	 * 
	 * @return true if the block is recorded as being exploded.
	 */
	public boolean isExploded(Block b) {
		return exploded.contains(b);
	}

	/**
	 * 
	 * 
	 * @param b
	 */
	public void addBlock(Block b) {
		exploded.add(b);
	}

	public boolean unexplode(Block b) {
		return exploded.remove(b);
	}

	/**
	 * Gets the MachineManager instance.
	 * 
	 * @return the MachineManager
	 */
	public static MachineManager getManager() {
		return SblockMachines.getMachines().getManager();
	}
}
