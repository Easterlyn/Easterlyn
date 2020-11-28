package com.easterlyn.util;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Enum for compass direction based on Player yaw.
 *
 * @author Jikoo
 */
public enum Direction {
  NORTH,
  EAST,
  SOUTH,
  WEST,
  UP,
  DOWN;

  /**
   * Get a Direction based on Player facing.
   *
   * @param player the Player
   * @return the Direction
   */
  public static Direction getFacingDirection(Player player) {
    return getFacingDirection(player.getLocation());
  }

  /**
   * Get a Direction based on Location yaw.
   *
   * @param location the Location
   * @return the Direction
   */
  public static Direction getFacingDirection(Location location) {
    byte playerFace = (byte) Math.round(location.getYaw() / 90);
    if (playerFace == 0 || playerFace == -4 || playerFace == 4) {
      return SOUTH;
    } else if (playerFace == 1 || playerFace == -3) {
      return WEST;
    } else if (playerFace == 2 || playerFace == -2) {
      return NORTH;
    } else {
      return EAST;
    }
  }

  public static Direction safeValue(@Nullable String directionName) {
    if (directionName == null) {
      return NORTH;
    }
    try {
      return Enum.valueOf(Direction.class, directionName);
    } catch (IllegalArgumentException e) {
      return NORTH;
    }
  }

  /**
   * Gets the Bukkit BlockFace corresponding with this Direction.
   *
   * @return the BlockFace
   */
  public BlockFace toBlockFace() {
    switch (this) {
      case EAST:
        return BlockFace.EAST;
      case NORTH:
        return BlockFace.NORTH;
      case SOUTH:
        return BlockFace.SOUTH;
      case WEST:
        return BlockFace.WEST;
      case UP:
        return BlockFace.UP;
      case DOWN:
        return BlockFace.DOWN;
      default:
        return BlockFace.SELF;
    }
  }

  /** Gets the Bukkit Axis corresponding with this Direction. * * @return the Axis */
  public Axis toAxis() {
    switch (this) {
      case NORTH:
      case SOUTH:
        return Axis.Z;
      case EAST:
      case WEST:
        return Axis.X;
      case UP:
      case DOWN:
      default:
        return Axis.Y;
    }
  }

  /**
   * For obtaining rotation based on original rotation - for stairs, etc. that are not facing in the
   * place Direction of the Machine.
   *
   * <p>The input Direction is the desired rotation for blocks relative to the original Direction.
   * The original Direction is assumed to be north relative to the new Direction, as all Shapes are
   * designed from a north-facing perspective.
   *
   * <p>Ex.: Machine placed west, block in machine faces east when machine is placed north.
   *
   * @param direction the Direction relative to this as north.
   */
  public Direction getRelativeDirection(Direction direction) {
    if (this.ordinal() > 3) {
      return this;
    }
    return values()[(this.ordinal() + direction.ordinal()) % 4];
  }
}
