/**
 * 
 */
package co.sblock.Sblock.Machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.Machines.Type.MachineType;
import co.sblock.Sblock.Machines.Type.PGOMachine;

/**
 * @author Jikoo
 *
 */
public class MachineManager {

	private Map<Location, Machine> machineKeys;
	private Map<Location, Location> machineBlocks;

	public MachineManager() {
		this.machineKeys = new HashMap<Location, Machine>();
		this.machineBlocks = new HashMap<Location, Location>();
	}

	public Machine addMachine(Location l, MachineType m, String data) {
		Machine machine = null;
		switch (m) {
		case ALCHEMITER:
			break;
		case APPEARIFIER:
			break;
		case COMPUTER:
			break;
		case CRUXTRUDER:
			break;
		case INTELLIBEAM_LASERSTATION:
			break;
		case PERFECTLY_GENERIC_OBJECT:
			machine = new PGOMachine(l, data);
		case PUNCH_DESIGNIX:
			break;
		case SENDIFICATOR:
			break;
		case TOTEM_LATHE:
			break;
		default:
			break;
		}
		machineKeys.put(l, machine);
		for (Location l1 : machine.getLocations()) {
			machineBlocks.put(l1, l);
		}
		return machine;
	}

	public void loadMachine(String location, String machineType, String data) {
		String[] l = location.split(",");
		addMachine(new Location(Bukkit.getWorld(l[0]), Integer.parseInt(l[1]),
				Integer.parseInt(l[2]), Integer.parseInt(l[3])), MachineType.getType(machineType), data);
		
	}

	public boolean isMachine(Location l) {
		return machineKeys.containsKey(l) || machineBlocks.containsKey(l);
	}

	public boolean isMachine(Block b) {
		return machineKeys.containsKey(b.getLocation()) || machineBlocks.containsKey(b.getLocation());
	}

	public Machine getMachineByBlock(Block b) {
		Machine m = machineKeys.get(b.getLocation());
		if (m == null) {
			m = machineKeys.get(machineBlocks.get(b.getLocation()));
		}
		return m;
	}

	public Set<Location> getMachines() {
		return machineKeys.keySet();
	}

	public List<Location> getMachines(MachineType m) {
		List<Location> list = new ArrayList<Location>();
		for (Entry<Location, Machine> e : machineKeys.entrySet()) {
			if (e.getValue().getType().equals(m)) {
				list.add(e.getKey());
			}
		}
		return list;
	}

	public void removeMachineListing(Location l) {
		if (machineKeys.containsKey(l)) {
			// TODO asynchronous
			DatabaseManager.getDatabaseManager().deleteMachine(machineKeys.remove(l));
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

	public void saveToDb() {
		for (Location l : machineKeys.keySet()) {
			DatabaseManager.getDatabaseManager().saveMachine(machineKeys.get(l));
		}
	}
}
