package co.sblock.users;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import co.sblock.data.SblockData;

/**
 * @author Jikoo
 */
public class TowerData {

    /** Map of Derse tower Locations. */
    private Map<Byte, Location> derse;
    /** Map of Prospit tower Locations. */
    private Map<Byte, Location> prospit;

    /** Constructor for TowerData. */
    public TowerData() {
        derse = new HashMap<Byte, Location>();
        prospit = new HashMap<Byte, Location>();
    }

    /**
     * Add a tower Location from a String.
     * 
     * @param planet the name of the DreamPlanet
     * @param location the String representation of a Location and tower number
     */
    public void add(String planet, String location) {
        String[] coords = location.split(",");
        Location l = new Location(Bukkit.getWorld(planet.contains("Derse") ?
                "OuterCircle" : "InnerCircle"), Double.parseDouble(coords[0]) + .5,
                Double.parseDouble(coords[1]), Double.parseDouble(coords[2]) + .5);
        if (planet.contains("Derse")) {
            derse.put(Byte.valueOf(planet.substring(5)), l);
        } else if (planet.contains("Prospit")) {
            prospit.put(Byte.valueOf(planet.substring(7)), l);
        }
    }

    /**
     * Add a tower Location from a String.
     * 
     * @param l the Location to add
     * @param number the tower number to set
     */
    public void add(Location l, byte number) {
        if (l.getWorld().getName().equals("OuterCircle")) {
            derse.put(number, l);
        } else if (l.getWorld().getName().equals("InnerCircle")) {
            prospit.put(number, l);
        }
    }

    /**
     * Gets the Location of a specific tower.
     * 
     * @param number the tower number to check
     * @param dPlanet the DreamPlanet of the tower
     * 
     * @return Location the Location of the tower
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
     * @param dPlanet the DreamPlanet of the tower
     * 
     * @return Location a valid location in the DreamPlanet's World
     */
    private Location findValidLocation(DreamPlanet dPlanet) {
        switch (dPlanet) {
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
     * Gets a String representation of a tower Location by planet and number.
     * 
     * @param world the name of the planet
     * @param key the tower number
     * 
     * @return a String representation of a Location.
     */
    public String getLocString(String world, byte key) {
        Location l;
        if (world.equals("Prospit")) {
            l = prospit.get(key);
        } else if (world.equals("Derse")) {
            l = derse.get(key);
        } else {
            return null;
        }
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
