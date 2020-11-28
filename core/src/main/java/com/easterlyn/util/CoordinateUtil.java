package com.easterlyn.util;

import com.easterlyn.util.tuple.Pair;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * A small class for converting coordinates between block, chunks, and regions.
 *
 * @author Jikoo
 */
public class CoordinateUtil {

  private static final Pattern REGION_FILE = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");

  private CoordinateUtil() {}

  /**
   * Converts region coordinates into chunk coordinates.
   *
   * @param region the coordinate to convert
   * @return the converted coordinate
   */
  public static int regionToChunk(final int region) {
    return region << 5;
  }

  /**
   * Converts region coordinates into block coordinates.
   *
   * @param region the coordinate to convert
   * @return the converted coordinate
   */
  public static int regionToBlock(final int region) {
    return region << 9;
  }

  /**
   * Converts chunk coordinates into region coordinates.
   *
   * @param chunk the coordinate to convert
   * @return the converted coordinate
   */
  public static int chunkToRegion(final int chunk) {
    return chunk >> 5;
  }

  /**
   * Converts chunk coordinates into block coordinates.
   *
   * @param chunk the coordinate to convert
   * @return the converted coordinate
   */
  public static int chunkToBlock(final int chunk) {
    return chunk << 4;
  }

  /**
   * Converts block coordinates into region coordinates.
   *
   * @param block the coordinate to convert
   * @return the converted coordinate
   */
  public static int blockToRegion(final int block) {
    return block >> 9;
  }

  /**
   * Converts block coordinates into chunk coordinates.
   *
   * @param block the coordinate to convert
   * @return the converted coordinate
   */
  public static int blockToChunk(final int block) {
    return block >> 4;
  }

  /**
   * Gets the lowest chunk coordinates of a region.
   *
   * @param regionFileName the name of the region file in r.X.Z.mca format
   * @return a Pair containing the X and Z coordinates of the lowest chunk in the region
   * @throws IllegalArgumentException if the region file name is not in the correct format
   */
  public static Pair<Integer, Integer> getRegionChunkCoords(final String regionFileName) {
    Matcher matcher = REGION_FILE.matcher(regionFileName);
    if (!matcher.find()) {
      throw new IllegalArgumentException(
          regionFileName + " does not match the region file name format!");
    }

    return new Pair<>(
        regionToChunk(Integer.parseInt(matcher.group(1))),
        regionToChunk(Integer.parseInt(matcher.group(2))));
  }

  /**
   * Creates a configuration-friendly path from a location.
   *
   * @param location the Location
   * @return the path created
   */
  public static String pathFromLoc(Location location) {
    if (location.getWorld() == null) {
      throw new IllegalArgumentException("Cannot get location path with null world!");
    }
    return location.getWorld().getName()
        + '.'
        + (location.getBlockX() >> 4)
        + '_'
        + (location.getBlockZ() >> 4)
        + '.'
        + location.getBlockX()
        + '_'
        + location.getBlockY()
        + '_'
        + location.getBlockZ();
  }

  /**
   * Gets a location from a configuration-friendly path.
   *
   * @param string the path
   * @return the Location created
   */
  public static Location locFromPath(String string) {
    String[] pathSplit = string.split("\\.");
    if (pathSplit.length < 3) {
      throw new IllegalArgumentException("Invalid location path: " + string);
    }
    String[] xyz = pathSplit[2].split("_");
    if (xyz.length < 3) {
      throw new IllegalArgumentException("Invalid location path: " + string);
    }
    return new Location(
        Bukkit.getWorld(pathSplit[0]),
        Integer.parseInt(xyz[0]),
        Integer.parseInt(xyz[1]),
        Integer.parseInt(xyz[2]));
  }
}
