package co.sblock.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SpawnLocationInformation
{
	protected static SpawnLocationInformation INSTANCE;
	
	protected SpawnLocationInformation() {
		INSTANCE = this;
	}
	
	public Location getSpawnLocationImpl()
	{
		return new Location(Bukkit.getWorld("Earth"), -3.5, 20, 6.5, 179.99F, 1F);
	}
	
	public static Location getSpawnLocation()
	{
		if (INSTANCE == null) {
			new SpawnLocationInformation();
		}
		return INSTANCE.getSpawnLocationImpl();
	}
}
