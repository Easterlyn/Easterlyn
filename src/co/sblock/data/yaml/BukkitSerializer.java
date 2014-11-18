package co.sblock.data.yaml;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * 
 * 
 * @author Jikoo
 */
public class BukkitSerializer {

	public static String locationToString(Location location) {
		return location.getWorld() + "," +  location.getX() + "," +  location.getY() + "," +  location.getZ();
	}

	public static Location locationFromString(String location) {
		String[] data = location.split(", ?");
		return new Location(Bukkit.getWorld(data[0]), Double.parseDouble(data[1]),
				Double.parseDouble(data[2]), Double.parseDouble(data[3]));
	}
}
