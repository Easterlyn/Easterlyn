/**
 * 
 */
package co.sblock.Sblock.UserData;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import co.sblock.Sblock.Database.SblockData;

/**
 * @author Jikoo
 */
public class TowerData {

	/** <code>Map</code> of Derse tower <code>Locations</code>. */
	private Map<Byte, Location> derse;
	/** <code>Map</code> of Prospit tower <code>Locations</code>. */
	private Map<Byte, Location> prospit;

	/**
	 * Constructor for <code>TowerData</code>.
	 */
	public TowerData() {
		derse = new HashMap<Byte, Location>();
		prospit = new HashMap<Byte, Location>();
	}

	/**
	 * Add a tower <code>Location</code> from a <code>String</code>.
	 * 
	 * @param planet
	 *            the name of the <code>DreamPlanet</code>
	 * @param location
	 *            the <code>String</code> representation of a
	 *            <code>Location</code> and tower number
	 */
	public void add(String planet, String location) {
		String[] coords = location.split(",");
		Location l = new Location(
				Bukkit.getWorld(planet.contains("Derse") ?
						"OuterCircle" : "InnerCircle"),
				Double.parseDouble(coords[0]) + .5,
				Double.parseDouble(coords[1]),
				Double.parseDouble(coords[2]) + .5);
		if (planet.contains("Derse")) {
			derse.put(Byte.valueOf(planet.substring(5)), l);
		} else  if (planet.contains("Prospit")){
			prospit.put(Byte.valueOf(planet.substring(7)), l);
		}
	}

	/**
	 * Add a tower <code>Location</code> from a <code>String</code>.
	 * 
	 * @param l
	 *            the <code>Location</code> to add
	 * @param number
	 *            the tower number to set
	 */
	public void add(Location l, byte number) {
		if (l.getWorld().getName().equals("OuterCircle")) {
			derse.put(number, l);
		} else if (l.getWorld().getName().equals("InnerCircle")) {
			prospit.put(number, l);
		}
	}

	/**
	 * Gets the <code>Location</code> of a specific tower.
	 * 
	 * @param number
	 *            the tower number to check
	 * @param dPlanet
	 *            the <code>DreamPlanet</code> of the tower
	 * @return <code>Location</code> the <code>Location</code> of the tower
	 */
	public Location getLocation(byte number, DreamPlanet dPlanet) {
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
			return findValidLocation(dPlanet);
		}
	}

	/**
	 * @param dPlanet
	 * the <code>DreamPlanet</code> of the tower
	 * @return Location a valid location in the <code>DreamPlanet</code>'s <code>World</code>
	 */
	private Location findValidLocation(DreamPlanet dPlanet) {
		switch(dPlanet) {
		case DERSE:
			for (Location l : derse.values()) {
				if (l != null) {
					return l;
				}
			}
			return Bukkit.getWorld("OuterCircle").getSpawnLocation();
		case PROSPIT:
			for (Location l : prospit.values()) {
				if (l != null) {
					return l;
				}
			}
			return Bukkit.getWorld("InnerCircle").getSpawnLocation();
		default:
			return Bukkit.getWorld("Earth").getSpawnLocation();
		}
	}

	/**
	 * Gets a <code>String</code> representation of a tower
	 * <code>Location</code> by planet and number.
	 * 
	 * @param world
	 *            the name of the planet
	 * @param key
	 *            the tower number
	 * @return a <code>String</code> representation of a <code>Location</code>.
	 */
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

	/**
	 * Load all tower data from the database.
	 */
	public void load() {
		SblockData.getDB().loadTowerData();
	}

	/**
	 * Save all tower data to the database.
	 */
	public void save() {
		SblockData.getDB().saveTowerData(this);
	}
}
