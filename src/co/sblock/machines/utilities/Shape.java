package co.sblock.machines.utilities;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * A blank structure for building multi-block Machines.
 * 
 * @author Jikoo
 */
public class Shape {

    /** All relative Locations and Materials of the Machine. */
    private HashMap<Vector, MaterialData> vectors;

    /** The key Location of the Machine. */
    private Location key;

    /**
     * Constructor of Shape. Creates a blank Shape.
     * 
     * @param l the location the Machine is placed in
     */
    public Shape(Location l) {
        this.key = l;
        this.vectors = new HashMap<>();
    }

    /**
     * Adds a Block to the defined Shape of a Machine.
     * <p>
     * N.B. all Locations are relative to the key Block of the Machine.
     * 
     * @param key the Location to add
     * @param m the MaterialData to make the Block from
     */
    public void addBlock(Vector v, MaterialData m) {
        // x axis is inverted for our interpretation of north on an x, z plane
        v.setX(-v.getX());
        this.vectors.put(v, m);
    }

    /**
     * Gets a HashMap of all properly oriented Locations and Materials needed to
     * build a Machine.
     * 
     * @param d the Direction the Machine needs to be built in
     * 
     * @return the Locations and relative MaterialData
     */
    public HashMap<Location, MaterialData> getBuildLocations(Direction d) {
        return assembly(rotate(d));
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
            return vectors;
        }
    }

    /**
     * Creates a copy of blocks with all locations rotated 90 degrees clockwise.
     * 
     * @return the clockwise rotation of blocks
     */
    private HashMap<Vector, MaterialData> rotateCW() {
        HashMap<Vector, MaterialData> newVectors = new HashMap<>();
        for (Entry<Vector, MaterialData> e : vectors.entrySet()) {
            Vector newVec = e.getKey().clone();
            int newZ = -newVec.getBlockX();
            newVec.setX(newVec.getBlockZ());
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
    private HashMap<Vector,MaterialData> rotateCCW() {
        HashMap<Vector, MaterialData> newVectors = new HashMap<>();
        for (Entry<Vector, MaterialData> e : vectors.entrySet()) {
            Vector newVec = e.getKey().clone();
            int newZ = newVec.getBlockX();
            newVec.setX(-newVec.getBlockZ());
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
    private HashMap<Vector, MaterialData> rotate180() {
        HashMap<Vector, MaterialData> newBlocks = new HashMap<>();
        for (Entry<Vector, MaterialData> e : vectors.entrySet()) {
            Vector newVec = e.getKey().clone();
            newVec.setX(-newVec.getBlockX());
            newVec.setZ(-newVec.getBlockZ());
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
    private HashMap<Location, MaterialData> assembly(HashMap<Vector, MaterialData> translation) {
        HashMap<Location, MaterialData> newLocs = new HashMap<>();
        for (Vector v : translation.keySet()) {
            newLocs.put(key.clone().add(v), translation.get(v));
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
