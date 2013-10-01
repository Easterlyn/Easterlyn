/**
 * 
 */
package co.sblock.Sblock.Machines.Type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author Jikoo
 *
 */
public class Computer extends Machine {

	/**
	 * @param l
	 *  location placed
	 * @param data
	 * name of the player who placed the computer
	 */
	Computer(Location l, String data) {
		super(l, data);
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#assemble()
	 */
	@Override
	public void assemble(BlockPlaceEvent event) {
		// Machine is single block, nothing to do!
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(org.bukkit.event.block.BlockBreakEvent)
	 */
	@Override
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return event.getPlayer().getName().equals(getData());
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#getLocations()
	 */
	@Override
	public List<Location> getLocations() {
		return new ArrayList<Location>();
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.COMPUTER;
	}
}
