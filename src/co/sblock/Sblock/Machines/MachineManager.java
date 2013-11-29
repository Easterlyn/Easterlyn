package co.sblock.Sblock.Machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Database.DBManager;
import co.sblock.Sblock.Machines.Type.Computer;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.Machines.Type.MachineType;
import co.sblock.Sblock.Machines.Type.PGO;
import co.sblock.Sblock.Machines.Type.Transportalizer;
import co.sblock.Sblock.Machines.Type.Shape.Direction;

/**
 * @author Jikoo
 */
public class MachineManager {

	/**
	 * A <code>Map</code> of <code>Machine</code> key <code>Locations</code> to
	 * corresponding <code>Machine</code>.
	 */
	private Map<Location, Machine> machineKeys;
	/**
	 * A Map of <code>Machine</code> <code>Block</code> <code>Location</code>s
	 * to the corresponding key <code>Location</code>.
	 */
	private Map<Location, Location> machineBlocks;

	public MachineManager() {
		this.machineKeys = new HashMap<Location, Machine>();
		this.machineBlocks = new HashMap<Location, Location>();
	}

	/**
	 * Adds a <code>Machine</code> with the given parameters.
	 * 
	 * @param l
	 *            the <code>Location</code> of the key
	 * @param m
	 *            the <code>MachineType</code>
	 * @param data
	 *            the additional <code>Machine</code> data
	 * @param d
	 *            the facing direction
	 * @return the <code>Machine</code> created
	 */
	public Machine addMachine(Location l, MachineType m, String data, Direction d) {
		Machine machine = null;
		switch (m) {
		case ALCHEMITER:
			break;
		case COMPUTER:
			machine = new Computer(l, data);
		case CRUXTRUDER:
			break;
		case INTELLIBEAM_LASERSTATION:
			break;
		case PERFECTLY_GENERIC_OBJECT:
			machine = new PGO(l, data);
		case PUNCH_DESIGNIX:
			break;
		case TOTEM_LATHE:
			break;
		case TRANSMATERIALIZER:
			break;
		case TRANSPORTALIZER:
			machine = new Transportalizer(l, data, d);
			break;
		default:
			break;
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
	 * @param location
	 *            the <code>Location</code> <code>String</code>
	 * @param machineType
	 *            the <code>MachineType</code> <code>String</code>
	 * @param data
	 *            the additional <code>Machine</code> data
	 * @param direction
	 *            the facing direction
	 */
	public void loadMachine(String location, String machineType, String data, byte direction) {
		String[] l = location.split(",");
		addMachine(new Location(Bukkit.getWorld(l[0]), Integer.parseInt(l[1]),
				Integer.parseInt(l[2]), Integer.parseInt(l[3])),
				MachineType.getType(machineType), data, Direction.getDirection(direction));
		
	}

	/**
	 * Checks a <code>Location</code> to see if there is a <code>Machine</code>
	 * there.
	 * 
	 * @param l
	 *            the <code>Location</code> to check
	 * @return true if the <code>Location</code> is a <code>Machine</code>
	 */
	public boolean isMachine(Location l) {
		return machineKeys.containsKey(l) || machineBlocks.containsKey(l);
	}

	/**
	 * Checks a <code>Block</code> to see if it is part of a
	 * <code>Machine</code>.
	 * 
	 * @param b
	 *            the <code>Block</code> to check
	 * @return true if the <code>Block</code> is a <code>Machine</code>
	 */
	public boolean isMachine(Block b) {
		return machineKeys.containsKey(b.getLocation()) || machineBlocks.containsKey(b.getLocation());
	}

	/**
	 * Gets a <code>Machine</code> from a <code>Block</code>.
	 * 
	 * @param b
	 *            the <code>Block</code>
	 * @return the <code>Machine</code>
	 */
	public Machine getMachineByBlock(Block b) {
		Machine m = machineKeys.get(b.getLocation());
		if (m == null) {
			m = machineKeys.get(machineBlocks.get(b.getLocation()));
		}
		return m;
	}

	/**
	 * Gets a <code>Machine</code> from a <code>Location</code>.
	 * 
	 * @param l
	 *            the <code>Location</code>
	 * @return the <code>Machine</code>
	 */
	public Machine getMachineByLocation(Location l) {
		Machine m = machineKeys.get(l);
		if (m == null) {
			m = machineKeys.get(machineBlocks.get(l));
		}
		return m;
	}

	/**
	 * Gets a <code>Set</code> of all <code>Machine</code> key
	 * <code>Locations</code>.
	 * 
	 * @return the <code>Set<Location></code>
	 */
	public Set<Location> getMachines() {
		return machineKeys.keySet();
	}

	/**
	 * Gets a <code>Set</code> of all <code>Machine</code> key
	 * <code>Locations</code> by <code>MachineType</code>.
	 * 
	 * @param m
	 *            the <code>MachineType</code>
	 * @return the <code>Set<Location></code>
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
	 * Remove the specified <code>Machine</code> listing.
	 * <p>
	 * Be aware - this does not modify the <code>World</code>. All
	 * <code>Block</code>s will remain.
	 * 
	 * @param l
	 *            the key <code>Location</code>
	 */
	public void removeMachineListing(Location l) {
		if (machineKeys.containsKey(l)) {
			DBManager.getDBM().deleteMachine(machineKeys.remove(l));
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
	 * Save all stored <code>Machine</code>s to the database.
	 */
	public void saveToDb() {
		for (Location l : machineKeys.keySet()) {
			DBManager.getDBM().saveMachine(machineKeys.get(l));
		}
	}

	/**
	 * Check if a <code>Player</code> is within the specified radius of their
	 * <code>Computer</code>.
	 * 
	 * @param p
	 *            the <code>Player</code>
	 * @param distance
	 *            the radius to search
	 * @return true if the <code>Player</code> is within the radius
	 */
	public boolean isByComputer(Player p, int distance) {
		for (Machine m : this.getMachinesInProximity(p.getLocation(), distance, MachineType.COMPUTER, true)) {
			if (m.getData().equals(p.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a <code>Set</code> of <code>Machine</code>s within a specified
	 * radius. If no <code>Machine</code>s match the specified conditions, an
	 * empty <code>Set</code> is returned.
	 * 
	 * @param current
	 *            the current <code>Location</code> (presumably of a
	 *            <code>Player</code>)
	 * @param searchDistance
	 *            the radius from current that is acceptable
	 * @param keyRequired
	 *            <code>true</code> if the key <code>Location</code> must be
	 *            within the radius. Less intensive search.
	 * @param mt
	 *            the <code>MachineType</code> to search for
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
	 * Check to see if the <code>Player</code> in question has placed a
	 * <code>Computer</code>.
	 * 
	 * @param p
	 *            the <code>Player</code>
	 * @return true if the Player has placed a <code>Computer</code>
	 */
	public boolean hasComputer(Player p) {
		for (Machine m : machineKeys.values()) {
			if (m instanceof Computer && m.getData().equals(p.getName())) {
				return true;
			}
		}
		return false;
	}
}
