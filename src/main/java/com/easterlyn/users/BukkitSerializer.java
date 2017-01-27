package com.easterlyn.users;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * 
 * 
 * @author Jikoo
 */
public class BukkitSerializer {

	public static String locationToBlockCenterString(Location location) {
		if (location == null) {
			return "null";
		}
		return location.getWorld().getName() + "," +  (location.getBlockX() + .5) + "," +  location.getBlockY() + "," +  (location.getBlockZ() + .5);
	}

	public static Location locationFromString(String location) {
		if (location == null) {
			return null;
		}
		String[] data = location.split(", ?");
		if (data.length < 4) {
			return null;
		}
		return new Location(Bukkit.getWorld(data[0]), Double.parseDouble(data[1]),
				Double.parseDouble(data[2]), Double.parseDouble(data[3]));
	}

}
