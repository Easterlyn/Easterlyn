package co.sblock.Sblock.UserData;

/**
 * Represents a dream planet.
 * 
 * @author FireNG
 * @author Jikoo
 */
public enum DreamPlanet {

	NONE, PROSPIT, DERSE;

	/**
	 * Gets the display name.
	 *
	 * @return The display name of this planet.
	 */
	public String getDisplayName() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}

	/**
	 * Gets the planet.
	 *
	 * @param name the name of a planet
	 * @return the DreamPlanet
	 */
	public static DreamPlanet getPlanet(String name) {
		try {
			return DreamPlanet.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return DreamPlanet.NONE;
		}
	}
}
