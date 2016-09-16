package co.sblock.machines.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.sblock.Sblock;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;
import co.sblock.users.UserAspect;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Machine representing a quest bed.
 * 
 * @author Jikoo
 */
public class Questbed extends Machine {

	private static final Map<UserAspect, List<Material>> ASPECT_SUPPLIES = new HashMap<>();

	static {
		// Blood
		ASPECT_SUPPLIES.put(UserAspect.BLOOD, Arrays.asList(Material.WOOD_SWORD,
				Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD,
				Material.RAW_BEEF, Material.RAW_CHICKEN, Material.RABBIT, Material.MUTTON,
				Material.PORK));

		// Breath
		ASPECT_SUPPLIES.put(UserAspect.BREATH, Arrays.asList(Material.FEATHER,
				Material.GOLD_RECORD, Material.GREEN_RECORD, Material.RECORD_3, Material.RECORD_4,
				Material.RECORD_5, Material.RECORD_6, Material.RECORD_7, Material.RECORD_8,
				Material.RECORD_9, Material.RECORD_10, Material.RECORD_11, Material.RECORD_12));

		// Doom
		ASPECT_SUPPLIES.put(UserAspect.DOOM, Arrays.asList(Material.ROTTEN_FLESH, Material.BONE,
				Material.FERMENTED_SPIDER_EYE, Material.CHORUS_FRUIT, Material.TNT,
				Material.FIREBALL));

		// Heart
		ASPECT_SUPPLIES.put(UserAspect.HEART, Arrays.asList(Material.GOLDEN_APPLE,
				Material.RABBIT_STEW, Material.SHIELD, Material.LEATHER_HELMET,
				Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
				Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS,
				Material.IRON_BOOTS, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
				Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.GOLD_HELMET,
				Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS,
				Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS,
				Material.DIAMOND_BOOTS));

		// Hope - seeds?
		ASPECT_SUPPLIES.put(UserAspect.HOPE, Arrays.asList(Material.FIREWORK, Material.END_CRYSTAL,
				Material.EYE_OF_ENDER, Material.SIGN));

		// Life - hoes, crops, wool
		// Light - glowstone, other lights
		// Mind - skull, compass, written book, exp bottle
		// Rage - dragon breath, ?? (furnace parallel? smeltables = white hot rage?)
		// Space - beacon, nether star, elytra
		// Time - clock, redstone shit?, ??
		// Void - empty bucket, obsidian, glass?
	}

	public Questbed(Sblock plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Questbed");
		Shape shape = getShape();
		MaterialDataValue value = shape.new MaterialDataValue(Material.BED_BLOCK, Direction.NORTH, "bedfoot");
		shape.setVectorData(new Vector(0, 0, 0), value);
		value = shape.new MaterialDataValue(Material.BED_BLOCK, Direction.NORTH, "bedhead");
		shape.setVectorData(new Vector(0, 0, 1), value);
	}

	@Override
	public ItemStack getUniqueDrop() {
		// TODO Auto-generated method stub
		return null;
	}

}
