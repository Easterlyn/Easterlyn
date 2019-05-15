package com.easterlyn.utilities;

import com.easterlyn.utilities.tuple.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A small class for converting coordinates between block, chunks, and regions.
 *
 * @author Jikoo
 */
public class CoordinateConversions {

	private static final Pattern REGION_FILE = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");

	/**
	 * Converts region coordinates into chunk coordinates.
	 *
	 * @param region the coordinate to convert
	 *
	 * @return the converted coordinate
	 */
	public static int regionToChunk(final int region) {
		return region << 5;
	}

	/**
	 * Converts region coordinates into block coordinates.
	 *
	 * @param region the coordinate to convert
	 *
	 * @return the converted coordinate
	 */
	public static int regionToBlock(final int region) {
		return region << 9;
	}

	/**
	 * Converts chunk coordinates into region coordinates.
	 *
	 * @param chunk the coordinate to convert
	 *
	 * @return the converted coordinate
	 */
	public static int chunkToRegion(final int chunk) {
		return chunk >> 5;
	}

	/**
	 * Converts chunk coordinates into block coordinates.
	 *
	 * @param chunk the coordinate to convert
	 *
	 * @return the converted coordinate
	 */
	public static int chunkToBlock(final int chunk) {
		return chunk << 4;
	}

	/**
	 * Converts block coordinates into region coordinates.
	 *
	 * @param block the coordinate to convert
	 *
	 * @return the converted coordinate
	 */
	public static int blockToRegion(final int block) {
		return block >> 9;
	}

	/**
	 * Converts block coordinates into chunk coordinates.
	 *
	 * @param block the coordinate to convert
	 *
	 * @return the converted coordinate
	 */
	public static int blockToChunk(final int block) {
		return block >> 4;
	}

	/**
	 * Gets the lowest chunk coordinates of a region.
	 *
	 * @param regionFileName the name of the region file in r.X.Z.mca format
	 *
	 * @return a Pair containing the X and Z coordinates of the lowest chunk in the region
	 *
	 * @throws IllegalArgumentException if the region file name is not in the correct format
	 */
	public static Pair<Integer, Integer> getRegionChunkCoords(final String regionFileName) {
		Matcher matcher = CoordinateConversions.REGION_FILE.matcher(regionFileName);
		if (!matcher.find()) {
			throw new IllegalArgumentException(regionFileName + " does not match the region file name format!");
		}

		return new Pair<>(CoordinateConversions.regionToChunk(Integer.parseInt(matcher.group(1))),
				CoordinateConversions.regionToChunk(Integer.parseInt(matcher.group(2))));
	}

}
