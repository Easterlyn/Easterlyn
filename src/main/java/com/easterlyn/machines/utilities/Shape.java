package com.easterlyn.machines.utilities;

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

//		private MaterialData getSpecificData() {
//			// TODO migrate from Direction
//		}
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
		setVectorData(vector, new MaterialDataValue(data.getItemType(), data.getData()));
	}

	/**
	 * Gets a HashMap of all properly oriented Locations and Materials needed to
	 * build a Machine.
	 * 
	 * @param location the Location to center the Shape on
	 * @param direction the Direction the Machine needs to be built in
	 * 
	 * @return the Locations and relative MaterialData
	 */
	public HashMap<Location, MaterialData> getBuildLocations(Location location, Direction direction) {
		HashMap<Location, MaterialData> newLocs = new HashMap<>();
		for (Iterator<Entry<Vector, MaterialDataValue>> iterator = vectors.entrySet().iterator(); iterator.hasNext();) {
			Entry<Vector, MaterialDataValue> entry = iterator.next();
			newLocs.put(location.clone().add(getRelativeVector(direction, entry.getKey().clone())), entry.getValue().getRotatedData(direction));
		}
		return newLocs;
	}

	/**
	 * Gets a Vector translated from the internal Easterlyn representation.
	 * 
	 * Internally, we consider north to be positive Z, east to be positive X. In Minecraft, north is negative Z.
	 * @param direction the Direction
	 * @param vector the Vector to translate
	 * @return
	 */
	public static Vector getRelativeVector(Direction direction, Vector vector) {
		switch (direction) {
		case EAST:
			double newZ = vector.getX();
			vector.setX(vector.getZ());
			vector.setZ(newZ);
			return vector;
		case SOUTH:
			vector.setX(-2 * vector.getBlockX() + vector.getX());
			int blockZ = (int) vector.getZ();
			vector.setZ(blockZ + vector.getZ() - blockZ);
			return vector;
		case WEST:
			double newZ1 = -2 * vector.getBlockX() + vector.getX();
			vector.setX(-2 * vector.getBlockZ() + vector.getZ());
			vector.setZ(newZ1);
			return vector;
		case NORTH:
		default:
			vector.setZ(-2 * vector.getBlockZ() + vector.getZ());
			return vector;
		}
	}

}
