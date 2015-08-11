package co.sblock.machines.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;

/**
 * Automation at its finest!
 * 
 * @author Jikoo
 */
public class CompilationAmalgamator extends Machine {

	private final ItemStack drop;

	CompilationAmalgamator(Location key, String owner, Direction direction) {
		super(new Shape());
		Shape shape = getShape();
		MaterialDataValue m = shape.new MaterialDataValue(Material.HOPPER);
		shape.setVectorData(new Vector(0, 2, 0), m);
		shape.setVectorData(new Vector(0, 0, 0), m);
		m = shape.new MaterialDataValue(Material.DROPPER);
		shape.setVectorData(new Vector(0, 1, 0), m);

		drop = null; // future
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}
}
