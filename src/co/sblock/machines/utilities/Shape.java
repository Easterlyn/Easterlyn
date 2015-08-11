package co.sblock.machines.utilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * A blank structure for building multi-block Machines.
 * 
 * @author Jikoo
 */
public class Shape {

	public class MaterialDataValue {
		Material material;
		byte data;
		Direction direction;
		String type;

		public MaterialDataValue(Material material, Direction direction, String type) {
			this.material = material;
			this.direction = direction;
			this.type = type;
		}

		public MaterialDataValue(Material material, byte data) {
			this.material = material;
			this.data = data;
		}

		public MaterialDataValue(Material material) {
			this.material = material;
			this.data = 0;
		}

		@SuppressWarnings("deprecation")
		public MaterialData getRotatedData(Direction direction) {
			if (this.direction == null) {
				return new MaterialData(material, data);
			}
			return new MaterialData(material, direction.getRelativeDirection(this.direction).getTypeByte(type));
		}
	}

	/** All relative Locations and Materials of the Machine. */
	private final HashMap<Vector, MaterialDataValue> vectors;

	/**
	 * Constructor of Shape. Creates a blank Shape.
	 * 
	 * @param l the location the Machine is placed in
	 */
	public Shape() {
		this.vectors = new HashMap<>();
	}

	/**
	 * Sets the MaterialData for a Vector.
	 * 
	 * @param vector the Vector
	 * @param data the MaterialData
	 */
	public void setVectorData(Vector vector, MaterialDataValue data) {
		vectors.put(vector, data);
	}

	/**
	 * Sets the MaterialData for a Vector.
	 * 
	 * @param vector the Vector
	 * @param data the MaterialData
	 */
	@SuppressWarnings("deprecation")
	public void setVectorData(Vector vector, MaterialData data) {
		vectors.put(vector, new MaterialDataValue(data.getItemType(), data.getData()));
	}

	/**
	 * Gets a HashMap of all properly oriented Locations and Materials needed to
	 * build a Machine.
	 * 
	 * @param location the Location to center the Shape on
	 * @param d the Direction the Machine needs to be built in
	 * 
	 * @return the Locations and relative MaterialData
	 */
	public HashMap<Location, MaterialData> getBuildLocations(Location location, Direction d) {
		return assembly(location, rotate(d));
	}

	/**
	 * Rotates block shape based on input Direction.
	 * 
	 * @param d the Direction to rotate to
	 * 
	 * @return the rotated shape
	 */
	private HashMap<Vector, MaterialData> rotate(Direction d) {
		switch (d) {
		case EAST:
			return rotateCW();
		case NORTH:
			return rotate180();
		case WEST:
			return rotateCCW();
		default:
			return current();
		}
	}

	/**
	 * Creates a copy of blocks with all locations rotated 90 degrees clockwise.
	 * 
	 * @return the clockwise rotation of blocks
	 */
	private HashMap<Vector, MaterialData> rotateCW() {
		HashMap<Vector, MaterialData> newVectors = new HashMap<>();
		for (Iterator<Entry<Vector, MaterialDataValue>> iterator = vectors.entrySet().iterator(); iterator.hasNext();) {
			Entry<Vector, MaterialDataValue> entry = iterator.next();
			Vector vector = entry.getKey().clone();
			int newZ = -vector.getBlockX();
			vector.setX(vector.getBlockZ());
			vector.setZ(newZ);
			newVectors.put(vector, entry.getValue().getRotatedData(Direction.EAST));
		}
		return newVectors;
	}

	/**
	 * Creates a copy of blocks with all locations rotated 90 degrees counterclockwise.
	 * 
	 * @return the counterclockwise rotation of blocks
	 */
	private HashMap<Vector, MaterialData> rotateCCW() {
		HashMap<Vector, MaterialData> newVectors = new HashMap<>();
		for (Iterator<Entry<Vector, MaterialDataValue>> iterator = vectors.entrySet().iterator(); iterator.hasNext();) {
			Entry<Vector, MaterialDataValue> entry = iterator.next();
			Vector vector = entry.getKey().clone();
			int newZ = vector.getBlockX();
			vector.setX(-vector.getBlockZ());
			vector.setZ(newZ);
			newVectors.put(vector, entry.getValue().getRotatedData(Direction.WEST));
		}
		return newVectors;
	}

	/**
	 * Creates a copy of blocks with all locations rotated 180 degrees.
	 * 
	 * @return the 180 degree rotation of blocks
	 */
	private HashMap<Vector, MaterialData> rotate180() {
		HashMap<Vector, MaterialData> newVectors = new HashMap<>();
		for (Iterator<Entry<Vector, MaterialDataValue>> iterator = vectors.entrySet().iterator(); iterator.hasNext();) {
			Entry<Vector, MaterialDataValue> entry = iterator.next();
			Vector vector = entry.getKey().clone();
			vector.setX(-vector.getBlockX());
			vector.setZ(-vector.getBlockZ());
			newVectors.put(vector, entry.getValue().getRotatedData(Direction.NORTH));
		}
		return newVectors;
	}

	/**
	 * Creates a copy of blocks.
	 * 
	 * @return the blocks
	 */
	private HashMap<Vector, MaterialData> current() {
		HashMap<Vector, MaterialData> newVectors = new HashMap<>();
		for (Iterator<Entry<Vector, MaterialDataValue>> iterator = vectors.entrySet().iterator(); iterator.hasNext();) {
			Entry<Vector, MaterialDataValue> entry = iterator.next();
			newVectors.put(entry.getKey().clone(), entry.getValue().getRotatedData(Direction.SOUTH));
		}
		return newVectors;
	}

	/**
	 * Creates a HashMap of in-world Locations for Machine components.
	 * 
	 * @param translation the correctly rotated Shape HashMap
	 * 
	 * @return valid ingame coordinates for assembling a Machine in
	 */
	private HashMap<Location, MaterialData> assembly(Location location, HashMap<Vector, MaterialData> translation) {
		HashMap<Location, MaterialData> newLocs = new HashMap<>();
		for (Entry<Vector, MaterialData> entry : translation.entrySet()) {
			newLocs.put(location.clone().add(entry.getKey()), entry.getValue());
		}
		return newLocs;
	}

	public static Vector getRelativeVector(Direction d, Vector v) {
		switch (d) {
		case EAST:
			double newZ = v.getX();
			v.setX(v.getBlockZ());
			v.setZ(newZ);
			return v;
		case SOUTH:
			v.setX(-v.getX());
			v.setZ(-v.getZ());
			return v;
		case WEST:
			double newZ1 = -v.getX();
			v.setX(v.getBlockZ());
			v.setZ(newZ1);
			return v;
		case NORTH:
		default:
			v.setX(-v.getX());
			return v;
		}
	}
}
