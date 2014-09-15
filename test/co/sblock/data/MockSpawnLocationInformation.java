package co.sblock.data;

import org.bukkit.Location;

import co.sblock.utilities.SpawnLocationInformation;

public class MockSpawnLocationInformation extends SpawnLocationInformation 
{
	protected MockSpawnLocationInformation() {
		INSTANCE = this;
	}
	
	public Location getSpawnLocationImpl()
	{
		return null;
	}
}
