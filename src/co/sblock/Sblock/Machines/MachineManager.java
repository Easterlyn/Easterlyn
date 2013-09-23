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

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * @author Jikoo
 *
 */
public class MachineManager {

	private Map<Location, MachineType> machines;

	public MachineManager() {
		this.machines = new HashMap<Location, MachineType>();
	}

	public void addMachine(Location l, MachineType m) {
		machines.put(l, m);
	}

	public void addMachine(Location[] l, MachineType m) {
		for (Location l1 : l) {
			machines.put(l1, m);
		}
	}
	// TODO addMachine for:
	// vectors from location
	// ArrayList<Location>
	// etc.

	public boolean isMachine(Location l) {
		return machines.containsKey(l);
	}

	public boolean isMachine(Block b) {
		return machines.containsKey(b.getLocation());
	}

	public Set<Location> getMachines() {
		return machines.keySet();
	}

	public List<Location> getMachines(MachineType m) {
		List<Location> list = new ArrayList<Location>();
		for (Entry<Location, MachineType> e : machines.entrySet()) {
			if (e.getValue().equals(m)) {
				list.add(e.getKey());
			}
		}
		return list;
	}
}
