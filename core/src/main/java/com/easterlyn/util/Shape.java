package com.easterlyn.util;

import com.easterlyn.event.ReportableEvent;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

/**
 * A utility for creating multi-block shapes.
 *
 * @author Jikoo
 */
public class Shape {

  /** All relative Locations and Materials of the Machine. */
  private final LinkedHashMap<Vector, MaterialDataValue> vectors;

  /** Constructor of Shape. Creates a blank Shape. */
  public Shape() {
    this.vectors = new LinkedHashMap<>();
  }

  /**
   * Gets a Vector translated from the internal Easterlyn representation.
   *
   * <p>Internally, we consider north to be positive Z, east to be positive X. In Minecraft, north
   * is negative Z.
   *
   * @param direction the Direction
   * @param vector the Vector to translate
   * @return the new Vector
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
   */
  public void setVectorData(Vector vector, Material data) {
    setVectorData(vector, new MaterialDataValue(data));
  }

  /**
   * Gets a Map of all properly oriented Blocks and MaterialDataValues needed to build a Shape.
   *
   * @param key the Block to center the Shape on
   * @param direction the Direction the Shape should be rotated
   * @return the Blocks and relative MaterialData
   */
  public Map<Block, MaterialDataValue> getBuildLocations(Block key, Direction direction) {
    Map<Block, MaterialDataValue> newLocs = new HashMap<>();
    for (Entry<Vector, MaterialDataValue> entry : vectors.entrySet()) {
      Vector relativeVector = getRelativeVector(direction, entry.getKey().clone());
      MaterialDataValue translated = new MaterialDataValue(entry.getValue().material);
      if (entry.getValue().transforms != null) {
        entry
            .getValue()
            .transforms
            .forEach(
                (data, dataDirection) ->
                    translated.withBlockData(data, dataDirection.getRelativeDirection(direction)));
      }
      newLocs.put(
          key.getRelative(
              relativeVector.getBlockX(), relativeVector.getBlockY(), relativeVector.getBlockZ()),
          translated);
    }
    return newLocs;
  }

  public void build(Block key, Direction direction) {
    for (Entry<Block, MaterialDataValue> entry :
        this.getBuildLocations(key, direction).entrySet()) {
      entry.getValue().build(entry.getKey());
    }
  }

  public static class MaterialDataValue {

    final Material material;
    @Nullable Multimap<Class<? extends BlockData>, Direction> transforms;

    public MaterialDataValue(Material material) {
      this.material = material;
      this.transforms = null;
    }

    public MaterialDataValue withBlockData(Class<? extends BlockData> clazz, Direction direction) {
      // TODO allow additional non-directional transforms via BlockData and Consumer
      if (transforms == null) {
        transforms = HashMultimap.create();
      }
      if (!transforms.containsEntry(clazz, direction)) {
        transforms.put(clazz, direction);
      }
      return this;
    }

    public Material getMaterial() {
      return this.material;
    }

    public void build(Block block) {
      block.setType(this.material, false);
      if (this.transforms == null) {
        return;
      }

      BlockData data = block.getBlockData();
      boolean dirty = false;

      for (Class<? extends BlockData> key : this.transforms.keySet()) {
        Collection<Direction> directions = this.transforms.get(key);

        if (Bisected.class.isAssignableFrom(key) && data instanceof Bisected) {
          ((Bisected) data)
              .setHalf(
                  directions.iterator().next() == Direction.UP
                      ? Bisected.Half.TOP
                      : Bisected.Half.BOTTOM);
          dirty = true;
        }

        if (Directional.class.isAssignableFrom(key) && data instanceof Directional) {
          Directional directional = (Directional) data;
          BlockFace faceDirection = directions.iterator().next().toBlockFace();
          if (!directional.getFaces().contains(faceDirection)) {
            ReportableEvent.call(
                String.format(
                    "Invalid facing %s for material %s. Valid faces: %s",
                    faceDirection.name(), this.material.name(), directional.getFaces()));
            faceDirection = directional.getFaces().iterator().next();
          }
          directional.setFacing(faceDirection);
          dirty = true;
        }

        if (Orientable.class.isAssignableFrom(key) && data instanceof Orientable) {
          Orientable orientable = (Orientable) data;
          Axis axis = directions.iterator().next().toAxis();
          if (!orientable.getAxes().contains(axis)) {
            ReportableEvent.call(
                String.format(
                    "Invalid axis %s for material %s. Valid axes: %s",
                    axis.name(), this.material.name(), orientable.getAxes()));
            axis = orientable.getAxes().iterator().next();
          }
          orientable.setAxis(axis);
          dirty = true;
        }

        if (Rotatable.class.isAssignableFrom(key) && data instanceof Rotatable) {
          ((Rotatable) data).setRotation(directions.iterator().next().toBlockFace());
          dirty = true;
        }

        if (MultipleFacing.class.isAssignableFrom(key) && data instanceof MultipleFacing) {
          MultipleFacing multipleFacing = (MultipleFacing) data;
          Set<BlockFace> allowedFaces = multipleFacing.getAllowedFaces();
          for (Direction direction : directions) {
            BlockFace blockFace = direction.toBlockFace();
            if (!allowedFaces.contains(blockFace)) {
              ReportableEvent.call(
                  String.format(
                      "Invalid face %s for material %s. Valid faces: %s",
                      blockFace.name(), this.material.name(), allowedFaces));
              continue;
            }
            multipleFacing.setFace(blockFace, true);
            dirty = true;
          }
        }
      }

      if (dirty) {
        block.setBlockData(data, false);
      }
    }
  }
}
