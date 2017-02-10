package com.easterlyn.utilities;

/**
 * Enum managing worlds and resource packs.
 * 
 * @author Jikoo
 */
public class RegionUtils {

	public static boolean regionsMatch(String worldName, String otherWorldName) {
		worldName = worldName.toUpperCase().replace("_NETHER", "").replace("_THE_END", "");
		otherWorldName = otherWorldName.toUpperCase().replace("_NETHER", "").replace("_THE_END", "");
		return worldName.equals(otherWorldName);
	}

}
