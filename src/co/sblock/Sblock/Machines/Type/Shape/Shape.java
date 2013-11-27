package co.sblock.Sblock.Machines.Type.Shape;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * A blank structure for building multi-block Machines.
 * 
 * @author Jikoo
 */
public class Shape {

	/** All relative <code>Location</code>s and <code>Material</code>s of the <code>Machine</code>. */
	private HashMap<Location, ItemStack> blocks;

	/** The key <code>Location</code> of the <code>Machine</code>. */
	private Location key;

	/**
	 * Constructor of <code>Shape</code>. Creates a blank <code>Shape</code>.
	 * @param l
	 */
	public Shape(Location l) {
		this.key = l;
		this.blocks = new HashMap<Location, ItemStack>();
	}

	/**
	 * Adds a <code>Block</code> to the defined <code>Shape</code> of a
	 * <code>Machine</code>.
	 * <p>
	 * N.B. all <code>Location</code>s are relative to the key
	 * <code>Block</code> of the <code>Machine</code>.
	 * 
	 * @param l
	 *            the <code>Location</code> to add
	 * @param is
	 *            the <code>ItemStack</code> (<code>Material</code> and damage)
	 *            to make the <code>Block</code> from
	 */
	public void addBlock(Location l, ItemStack is) {
		this.blocks.put(l, is);
	}

	/**
	 * Gets a <code>HashMap</code> of all properly oriented
	 * <code>Location</code>s and <code>Material</code>s needed to build a
	 * <code>Machine</code>.
	 * 
	 * @param d
	 *            the <code>Direction</code> the <code>Machine</code> needs to
	 *            be built in
	 * @return the <code>Location</code>s and relative <code>ItemStack</code>s
	 */
	public HashMap<Location, ItemStack> getBuildLocations(Direction d) {
		return assembly(rotate(d));
	}

	/**
	 * Rotates block shape based on input <code>Direction</code>.
	 * 
	 * @param d
	 *            the <code>Direction</code> to rotate to
	 * @return the rotated shape
	 */
	private HashMap<Location, ItemStack> rotate(Direction d) {
		switch (d) {
		case WEST:
			return rotateCCW();
		case SOUTH:
			return rotate180();
		case EAST:
			return rotateCW();
		default:
			return blocks;
		}
	}

	/**
	 * Creates a copy of blocks with all locations rotated 90 degrees clockwise.
	 * 
	 * @return the clockwise rotation of blocks
	 */
	private HashMap<Location,ItemStack> rotateCW() {
		HashMap<Location, ItemStack> newBlocks = new HashMap<Location, ItemStack>();
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			Location newLoc = e.getKey().clone();
			int x = newLoc.getBlockX();
			int z = newLoc.getBlockZ();
			if (x == 0) {
				x = z;
				z = 0;
			} else if (x < 0) {
				if (z == 0) {
					z = x * -1;
					x = 0;
				} else if (z < 0) {
					z = z * -1;
				} else {
					x = x * -1;
				}
			} else {
				if (z == 0) {
					z = x * -1;
					x = 0;
				} else if (z < 0) {
					x = x * -1;
				} else {
					z = z * -1;
				}
			}
			newLoc.setX(x);
			newLoc.setZ(z);
			newBlocks.put(newLoc, e.getValue());
		}
		return newBlocks;
	}

	/**
	 * Creates a copy of blocks with all locations rotated 90 degrees counterclockwise.
	 * 
	 * @return the counterclockwise rotation of blocks
	 */
	private HashMap<Location,ItemStack> rotateCCW() {
		HashMap<Location, ItemStack> newBlocks = new HashMap<Location, ItemStack>();
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			Location newLoc = e.getKey().clone();
			int x = newLoc.getBlockX();
			int z = newLoc.getBlockZ();
			if (x == 0) {
				x = z * -1;
				z = 0;
			} else if (x < 0) {
				if (z == 0) {
					z = x;
					x = 0;
				} else if (z < 0) {
					x = x * -1;
				} else {
					z = z * -1;
				}
			} else {
				if (z == 0) {
					z = x;
					x = 0;
				} else if (z < 0) {
					z = z * -1;
				} else {
					x = x * -1;
				}
			}
			newLoc.setX(x);
			newLoc.setZ(z);
			newBlocks.put(newLoc, e.getValue());
		}
		return newBlocks;
	}

	/**
	 * Creates a copy of blocks with all locations rotated 180 degrees.
	 * 
	 * @return the 180 degree rotation of blocks
	 */
	private HashMap<Location, ItemStack> rotate180() {
		HashMap<Location, ItemStack> newBlocks = new HashMap<Location, ItemStack>();
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			Location newLoc = e.getKey().clone();
			newLoc.setX(newLoc.getBlockX() * -1);
			newLoc.setZ(newLoc.getBlockZ() * -1);
			newBlocks.put(newLoc, e.getValue());
		}
		return newBlocks;
	}

	/**
	 * Creates a <code>HashMap</code> of in-world <code>Location</code>s for
	 * <code>Machine</code> components.
	 * 
	 * @param translation
	 *            the correctly rotated <code>Shape</code> <code>HashMap</code>
	 * @return valid ingame coordinates for assembling a <code>Machine</code> in
	 */
	private HashMap<Location, ItemStack> assembly(HashMap<Location, ItemStack> translation) {
		HashMap<Location, ItemStack> newLocs = new HashMap<Location, ItemStack>();
		for (Location l : translation.keySet()) {
			newLocs.put(key.add(l), translation.get(l));
		}
		return newLocs;
	}
}
