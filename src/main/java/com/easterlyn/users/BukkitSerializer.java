package com.easterlyn.users;

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

}
