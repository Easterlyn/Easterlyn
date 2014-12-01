package co.sblock.machines.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.MachineType;

/**
 * 
 * 
 * @author Jikoo
 */
public class CompoundingUnionizor extends Machine {

	/**
	 * @param key
	 * @param owner
	 * @param direction
	 */
	CompoundingUnionizor(Location key, String owner, Direction direction) {
		super(key, owner, direction);
		MaterialData m = new MaterialData(Material.HOPPER);
		shape.addBlock(new Vector(0, 2, 0), m);
		shape.addBlock(new Vector(0, 0, 0), m);
		m = new MaterialData(Material.WORKBENCH);
		shape.addBlock(new Vector(0, 1, 0), m);
	}

	/* (non-Javadoc)
	 * @see co.sblock.machines.type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		// TODO Auto-generated method stub
		return null;
	}
}
