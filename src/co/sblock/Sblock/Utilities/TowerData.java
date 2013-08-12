/**
 * 
 */
package co.sblock.Sblock.Utilities;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.UserData.DreamPlanet;

/**
 * @author Jikoo
 *
 */
public class TowerData {

	private Map<Byte, Location> derse;
	private Map<Byte, Location> prospit;

	public TowerData() {
		derse = new HashMap<Byte, Location>();
		prospit = new HashMap<Byte, Location>();
	}

	public void add(String planet, String location) { // TODO fix
		String[] coords = location.split(",");
		Location l = new Location(
				Bukkit.getWorld(planet.substring(0, planet.length() - 2)),
				Double.parseDouble(coords[0]),
				Double.parseDouble(coords[1]),
				Double.parseDouble(coords[2]));
		if (planet.contains("Derse")) {
			derse.put(Byte.valueOf(planet.substring(5)), l);
		} else  if (planet.contains("Prospit")){
			prospit.put(Byte.valueOf(planet.substring(7)), l);
		}
	}

	public void add(Location l, byte number) {
		if (l.getWorld().getName().equals("OuterCircle")) {
			derse.put(number, l);
		} else if (l.getWorld().getName().equals("InnerCircle")) {
			prospit.put(number, l);
		}
	}

	public Location getLocation(byte number, DreamPlanet dPlanet, byte enterZeroHere) {
		if (enterZeroHere != 0) {
			if (enterZeroHere > 8) {
				return Bukkit.getWorld(dPlanet.getDisplayName()).getSpawnLocation();
			} else {
				number = (byte) (enterZeroHere - 1);
				enterZeroHere++;
			}
		} else {
			enterZeroHere = 1;
		}
		Location l;
		switch (dPlanet) {
		case DERSE:
			l = derse.get(number);
			break;
		case PROSPIT:
			l = prospit.get(number);
			break;
		default:
			return null;
		}
		if (l != null) {
			return l;
		} else {
			return this.getLocation(number, dPlanet, enterZeroHere);
		}
	}

	public String getLocString(String world, byte key) {
		Location l;
		if (world.equals("Prospit")) {
			l = prospit.get(key);
		} else  if (world.equals("Derse")) {
			l = derse.get(key);
		} else return null;
		if (l != null) {
			return l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
		} else {
			return null;
		}
	}

	public void load() {
		DatabaseManager.getDatabaseManager().loadTowerData();
	}

	public void save(TowerData towers) {
		DatabaseManager.getDatabaseManager().saveTowerData(towers);
	}
}
