package co.sblock.machines.type;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;

/**
 * Combine and create.
 * 
 * @author Jikoo
 */
public class CompoundingUnionizor extends Machine {

	private final ItemStack drop;

	public CompoundingUnionizor() {
		super(new Shape());
		Shape shape = getShape();
		MaterialDataValue m = shape.new MaterialDataValue(Material.HOPPER);
		shape.setVectorData(new Vector(0, 2, 0), m);
		shape.setVectorData(new Vector(0, 0, 0), m);
		m = shape.new MaterialDataValue(Material.WORKBENCH);
		shape.setVectorData(new Vector(0, 1, 0), m);

		drop = null; // future
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

}
