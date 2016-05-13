package co.sblock.machines.type;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;

/**
 * Automatic Anaash fun times.
 * 
 * @author Jikoo
 */
public class EnchantmentShrine extends Machine {

	@SuppressWarnings("deprecation")
	public EnchantmentShrine(Sblock plugin, Machines machines) {
		super(plugin, machines, new Shape(), "EnchantmentShrine");

		Shape shape = getShape();

		// Bottom base layer
		MaterialDataValue matData = shape.new MaterialDataValue(Material.OBSIDIAN);
		shape.setVectorData(new Vector(-3, 0, -3), matData);
		shape.setVectorData(new Vector(-2, 0, -3), matData);
		shape.setVectorData(new Vector(-1, 0, -3), matData);
		shape.setVectorData(new Vector(2, 0, -3), matData);
		shape.setVectorData(new Vector(3, 0, -3), matData);
		shape.setVectorData(new Vector(-1, 0, -2), matData);
		shape.setVectorData(new Vector(0, 0, -2), matData);
		shape.setVectorData(new Vector(2, 0, -2), matData);
		shape.setVectorData(new Vector(3, 0, -2), matData);
		shape.setVectorData(new Vector(-3, 0, -1), matData);
		shape.setVectorData(new Vector(-2, 0, -1), matData);
		shape.setVectorData(new Vector(0, 0, -1), matData);
		shape.setVectorData(new Vector(1, 0, -1), matData);
		shape.setVectorData(new Vector(2, 0, -1), matData);
		shape.setVectorData(new Vector(-1, 0, 0), matData);
		shape.setVectorData(new Vector(1, 0, 0), matData);
		shape.setVectorData(new Vector(-2, 0, 1), matData);
		shape.setVectorData(new Vector(-1, 0, 1), matData);
		shape.setVectorData(new Vector(0, 0, 1), matData);
		shape.setVectorData(new Vector(2, 0, 1), matData);
		shape.setVectorData(new Vector(3, 0, 1), matData);
		shape.setVectorData(new Vector(-3, 0, 2), matData);
		shape.setVectorData(new Vector(-2, 0, 2), matData);
		shape.setVectorData(new Vector(0, 0, 2), matData);
		shape.setVectorData(new Vector(1, 0, 2), matData);
		shape.setVectorData(new Vector(-3, 0, 3), matData);
		shape.setVectorData(new Vector(-2, 0, 3), matData);
		shape.setVectorData(new Vector(1, 0, 3), matData);
		shape.setVectorData(new Vector(2, 0, 3), matData);
		shape.setVectorData(new Vector(3, 0, 3), matData);

		matData = shape.new MaterialDataValue(Material.LAPIS_BLOCK);
		shape.setVectorData(new Vector(0, 0, -3), matData);
		shape.setVectorData(new Vector(1, 0, -3), matData);
		shape.setVectorData(new Vector(-3, 0, -2), matData);
		shape.setVectorData(new Vector(-2, 0, -2), matData);
		shape.setVectorData(new Vector(1, 0, -2), matData);
		shape.setVectorData(new Vector(-1, 0, -1), matData);
		shape.setVectorData(new Vector(3, 0, -1), matData);
		shape.setVectorData(new Vector(-3, 0, 0), matData);
		shape.setVectorData(new Vector(-2, 0, 0), matData);
		shape.setVectorData(new Vector(2, 0, 0), matData);
		shape.setVectorData(new Vector(3, 0, 0), matData);
		shape.setVectorData(new Vector(-3, 0, 1), matData);
		shape.setVectorData(new Vector(1, 0, 1), matData);
		shape.setVectorData(new Vector(-1, 0, 2), matData);
		shape.setVectorData(new Vector(2, 0, 2), matData);
		shape.setVectorData(new Vector(3, 0, 2), matData);
		shape.setVectorData(new Vector(-1, 0, 3), matData);
		shape.setVectorData(new Vector(0, 0, 3), matData);

		// Top base layer

		matData = shape.new MaterialDataValue(Material.STAINED_GLASS, DyeColor.BLACK.getWoolData());
		shape.setVectorData(new Vector(-3, 1, -3), matData);
		shape.setVectorData(new Vector(-2, 1, -3), matData);
		shape.setVectorData(new Vector(-1, 1, -3), matData);
		shape.setVectorData(new Vector(2, 1, -3), matData);
		shape.setVectorData(new Vector(3, 1, -3), matData);
		shape.setVectorData(new Vector(-1, 1, -2), matData);
		shape.setVectorData(new Vector(0, 1, -2), matData);
		shape.setVectorData(new Vector(2, 1, -2), matData);
		shape.setVectorData(new Vector(3, 1, -2), matData);
		shape.setVectorData(new Vector(-3, 1, -1), matData);
		shape.setVectorData(new Vector(-2, 1, -1), matData);
		shape.setVectorData(new Vector(1, 1, -1), matData);
		shape.setVectorData(new Vector(2, 1, -1), matData);
		shape.setVectorData(new Vector(-2, 1, 1), matData);
		shape.setVectorData(new Vector(-1, 1, 1), matData);
		shape.setVectorData(new Vector(2, 1, 1), matData);
		shape.setVectorData(new Vector(3, 1, 1), matData);
		shape.setVectorData(new Vector(-3, 1, 2), matData);
		shape.setVectorData(new Vector(-2, 1, 2), matData);
		shape.setVectorData(new Vector(0, 1, 2), matData);
		shape.setVectorData(new Vector(1, 1, 2), matData);
		shape.setVectorData(new Vector(-3, 1, 3), matData);
		shape.setVectorData(new Vector(-2, 1, 3), matData);
		shape.setVectorData(new Vector(1, 1, 3), matData);
		shape.setVectorData(new Vector(2, 1, 3), matData);
		shape.setVectorData(new Vector(3, 1, 3), matData);

		matData = shape.new MaterialDataValue(Material.STAINED_GLASS, DyeColor.BLUE.getWoolData());
		shape.setVectorData(new Vector(0, 1, -3), matData);
		shape.setVectorData(new Vector(1, 1, -3), matData);
		shape.setVectorData(new Vector(-3, 1, -2), matData);
		shape.setVectorData(new Vector(-2, 1, -2), matData);
		shape.setVectorData(new Vector(1, 1, -2), matData);
		shape.setVectorData(new Vector(-1, 1, -1), matData);
		shape.setVectorData(new Vector(3, 1, -1), matData);
		shape.setVectorData(new Vector(-3, 1, 0), matData);
		shape.setVectorData(new Vector(-2, 1, 0), matData);
		shape.setVectorData(new Vector(2, 1, 0), matData);
		shape.setVectorData(new Vector(3, 1, 0), matData);
		shape.setVectorData(new Vector(-3, 1, 1), matData);
		shape.setVectorData(new Vector(1, 1, 1), matData);
		shape.setVectorData(new Vector(-1, 1, 2), matData);
		shape.setVectorData(new Vector(2, 1, 2), matData);
		shape.setVectorData(new Vector(3, 1, 2), matData);
		shape.setVectorData(new Vector(-1, 1, 3), matData);
		shape.setVectorData(new Vector(0, 1, 3), matData);

		matData = shape.new MaterialDataValue(Material.HOPPER);
		shape.setVectorData(new Vector(0, 0, 0), matData);

		matData = shape.new MaterialDataValue(Material.CARPET, DyeColor.BLUE.getWoolData());
		shape.setVectorData(new Vector(0, 1, 0), matData);

		// Central stairs and decorative cover
		matData = shape.new MaterialDataValue(Material.NETHER_BRICK_STAIRS, Direction.NORTH, "stair");
		shape.setVectorData(new Vector(0, 1, 1), matData);
		shape.setVectorData(new Vector(0, 5, -1), matData);

		matData = shape.new MaterialDataValue(Material.NETHER_BRICK_STAIRS, Direction.EAST, "stair");
		shape.setVectorData(new Vector(1, 1, 0), matData);
		shape.setVectorData(new Vector(-1, 5, 0), matData);

		matData = shape.new MaterialDataValue(Material.NETHER_BRICK_STAIRS, Direction.SOUTH, "stair");
		shape.setVectorData(new Vector(0, 1, -1), matData);
		shape.setVectorData(new Vector(0, 5, 1), matData);

		matData = shape.new MaterialDataValue(Material.NETHER_BRICK_STAIRS, Direction.WEST, "stair");
		shape.setVectorData(new Vector(-1, 1, 0), matData);
		shape.setVectorData(new Vector(1, 5, 0), matData);

		matData = shape.new MaterialDataValue(Material.NETHER_FENCE);
		shape.setVectorData(new Vector(1, 2, 1), matData);
		shape.setVectorData(new Vector(1, 3, 1), matData);
		shape.setVectorData(new Vector(1, 4, 1), matData);
		shape.setVectorData(new Vector(1, 5, 1), matData);
		shape.setVectorData(new Vector(1, 2, -1), matData);
		shape.setVectorData(new Vector(1, 3, -1), matData);
		shape.setVectorData(new Vector(1, 4, -1), matData);
		shape.setVectorData(new Vector(1, 5, -1), matData);
		shape.setVectorData(new Vector(-1, 2, 1), matData);
		shape.setVectorData(new Vector(-1, 3, 1), matData);
		shape.setVectorData(new Vector(-1, 4, 1), matData);
		shape.setVectorData(new Vector(-1, 5, 1), matData);
		shape.setVectorData(new Vector(-1, 2, -1), matData);
		shape.setVectorData(new Vector(-1, 3, -1), matData);
		shape.setVectorData(new Vector(-1, 4, -1), matData);
		shape.setVectorData(new Vector(-1, 5, -1), matData);
		shape.setVectorData(new Vector(0, 5, 0), matData);
		shape.setVectorData(new Vector(0, 6, 0), matData);
	}

	@Override
	public ItemStack getUniqueDrop() {
		// TODO Auto-generated method stub
		return null;
	}

}
