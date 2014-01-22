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

	/** All relative Locations and Materials of the Machine. */
	private HashMap<Vector, ItemStack> vectors;

	/** The key Location of the Machine. */
	private Location key;

	/**
	 * Constructor of Shape. Creates a blank Shape.
	 * 
	 * @param l the location the Machine is placed in
	 */
	public Shape(Location l) {
		this.key = l;
		this.vectors = new HashMap<Vector, ItemStack>();
	}

	/**
	 * Adds a Block to the defined Shape of a Machine.
	 * <p>
	 * N.B. all Locations are relative to the key Block of the Machine.
	 * 
	 * @param l the Location to add
	 * @param is the ItemStack (Material and damage) to make the Block from
	 */
	public void addBlock(Vector v, ItemStack is) {
		this.vectors.put(v, is);
	}

	/**
	 * Gets a HashMap of all properly oriented Locations and Materials needed to
	 * build a Machine.
	 * 
	 * @param d the Direction the Machine needs to be built in
	 * 
	 * @return the Locations and relative ItemStacks
	 */
	public HashMap<Location, ItemStack> getBuildLocations(Direction d) {
		return assembly(rotate(d));
	}

	/**
	 * Rotates block shape based on input Direction.
	 * 
	 * @param d the Direction to rotate to
	 * 
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
			int newZ = - newVec.getBlockX();
			int newX = newVec.getBlockZ();
			newVec.setX(newX);
			newVec.setZ(newZ);
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
			int newZ = newVec.getBlockX();
			int newX = - newVec.getBlockZ();
			newVec.setX(newX);
			newVec.setZ(newZ);
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
	 * Creates a HashMap of in-world Locations for Machine components.
	 * 
	 * @param translation the correctly rotated Shape HashMap
	 * 
	 * @return valid ingame coordinates for assembling a Machine in
	 */
	private HashMap<Location, ItemStack> assembly(HashMap<Vector, ItemStack> translation) {
		HashMap<Location, ItemStack> newLocs = new HashMap<Location, ItemStack>();
		for (Vector v : translation.keySet()) {
			newLocs.put(key.clone().add(v), translation.get(v));
		}
		return newLocs;
	}
}
