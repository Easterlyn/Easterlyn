package co.sblock.Sblock.Machines.Type;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * A blank structure for building multi-block Machines.
 * 
 * @author Jikoo
 */
public class Shape {

	/** All relative <code>Location</code>s and <code>Material</code>s of the <code>Machine</code>. */
	private HashMap<Vector, ItemStack> vectors;

	/** The key <code>Location</code> of the <code>Machine</code>. */
	private Location key;

	/**
	 * Constructor of <code>Shape</code>. Creates a blank <code>Shape</code>.
	 * @param l
	 */
	public Shape(Location l) {
		this.key = l;
		this.vectors = new HashMap<Vector, ItemStack>();
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
	public void addBlock(Vector v, ItemStack is) {
		this.vectors.put(v, is);
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
	private HashMap<Vector, ItemStack> rotate(Direction d) {
		switch (d) {
		case EAST:
			return rotateCW();
		case NORTH:
			return rotate180();
		case WEST:
			return rotateCCW();
		default:
			return vectors;
		}
	}

	/**
	 * Creates a copy of blocks with all locations rotated 90 degrees clockwise.
	 * 
	 * @return the clockwise rotation of blocks
	 */
	private HashMap<Vector,ItemStack> rotateCW() {
		HashMap<Vector, ItemStack> newVectors = new HashMap<Vector, ItemStack>();
		for (Entry<Vector, ItemStack> e : vectors.entrySet()) {
			Vector newVec = e.getKey().clone();
			int x = newVec.getBlockX();
			int z = newVec.getBlockZ();
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
			newVec.setX(x);
			newVec.setZ(z);
			newVectors.put(newVec, e.getValue());
		}
		return newVectors;
	}

	/**
	 * Creates a copy of blocks with all locations rotated 90 degrees counterclockwise.
	 * 
	 * @return the counterclockwise rotation of blocks
	 */
	private HashMap<Vector,ItemStack> rotateCCW() {
		HashMap<Vector, ItemStack> newVectors = new HashMap<Vector, ItemStack>();
		for (Entry<Vector, ItemStack> e : vectors.entrySet()) {
			Vector newVec = e.getKey().clone();
			int x = newVec.getBlockX();
			int z = newVec.getBlockZ();
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
			newVec.setX(x);
			newVec.setZ(z);
			newVectors.put(newVec, e.getValue());
		}
		return newVectors;
	}

	/**
	 * Creates a copy of blocks with all locations rotated 180 degrees.
	 * 
	 * @return the 180 degree rotation of blocks
	 */
	private HashMap<Vector, ItemStack> rotate180() {
		HashMap<Vector, ItemStack> newBlocks = new HashMap<Vector, ItemStack>();
		for (Entry<Vector, ItemStack> e : vectors.entrySet()) {
			Vector newVec = e.getKey().clone();
			newVec.setX(newVec.getBlockX() * -1);
			newVec.setZ(newVec.getBlockZ() * -1);
			newBlocks.put(newVec, e.getValue());
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
	private HashMap<Location, ItemStack> assembly(HashMap<Vector, ItemStack> translation) {
		HashMap<Location, ItemStack> newLocs = new HashMap<Location, ItemStack>();
		for (Vector v : translation.keySet()) {
			newLocs.put(key.clone().add(v), translation.get(v));
		}
		return newLocs;
	}
}
