package co.sblock.users;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerialisableLocation
{
	private final String worldName;
	private final int x, y, z;
	
	public SerialisableLocation(Location loc)
	{
		worldName = loc.getWorld().getName();
		x = loc.getBlockX();
		y = loc.getBlockY();
		z = loc.getBlockZ();
	}
	
	public final Location asLocation()
	{
		return new Location(Bukkit.getWorld(worldName), x, y, z);
	}
}
