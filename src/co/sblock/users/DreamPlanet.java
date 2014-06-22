package co.sblock.users;

/**
 * Represents a dream planet.
 * 
 * @author FireNG, Jikoo, tmathmeyer
 */
public enum DreamPlanet {

	NONE("Earth", "None"),
	PROSPIT("InnerCircle", "Prospit"),
	DERSE("OuterCircle", "Derse");

	/* The names of the World. */
	private final String worldName;
	private final String displayName;

	/**
	 * Constructor for DreamPlanet.
	 * 
	 * @param worldName the name of the World used for this DreamPlanet.
	 */
	private DreamPlanet(String worldName, String displayName) {
		this.worldName = worldName;
		this.displayName = displayName;
	}

	/**
	 * Gets the DreamPlanet's display name.
	 * 
	 * @return The display name of this DreamPlanet.
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Gets the name of the World.
	 * 
	 * @return the World name
	 */
	public String getWorldName() {
		return this.worldName;
	}

	/**
	 * Gets a DreamPlanet by name.
	 * 
	 * @param name the name of a DreamPlanet
	 * 
	 * @return the DreamPlanet specified, DreamPlanet.NONE if invalid.
	 */
	public static DreamPlanet getPlanet(String name) {
		try {
			return DreamPlanet.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return DreamPlanet.NONE;
		}
	}
}
