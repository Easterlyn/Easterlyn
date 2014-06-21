package co.sblock.users;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Represents each planet in the Medium.
 * 
 * @author FireNG, Jikoo, tmathmeyer
 */
public enum MediumPlanet {

	LOFAF("Land of Frost and Frogs", new Location(Bukkit.getWorld("Medium"), 2756, 64, -2756)),
	LOHAC("Land of Heat and Clockwork", new Location(Bukkit.getWorld("Medium"), 2756, 64, -2756)),
	LOLAR("Land of Light and Rain", new Location(Bukkit.getWorld("Medium"), -2756, 64, 2756)),
	LOWAS("Land of Wind and Shade", new Location(Bukkit.getWorld("Medium"), 2756, 64, 2756));

	/* Planet Names */
	private final String longName;
	private final Location center;

	/**
	 * 
	 * @param longName the name of the world
	 * @param center the in-game center
	 */
	private MediumPlanet(String longName, Location center) {
		this.longName = longName;
		this.center = center;
	}

	/**
	 * Gets the short name of a MediumPlanet.
	 * 
	 * @return the short name
	 */
	public String getShortName() {
		return this.name();
	}

	/**
	 * Gets the long name of a MediumPlanet.
	 * 
	 * @return the long name
	 */
	public String getLongName() {
		return this.longName;
	}

	/**
	 * Gets the MediumPlanet.
	 * 
	 * @param name the name of a MediumPlanet
	 * 
	 * @return the MediumPlanet
	 */
	public static MediumPlanet getPlanet(String name) {
		try {
			return MediumPlanet.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return MediumPlanet.LOWAS;
		}
	}

	/**
	 * Gets the center point of the MediumPlanet. Note that this Location can be unsafe.
	 * 
	 * @return the Location
	 */
	public Location getCenter() {
		return center;
	}
}
