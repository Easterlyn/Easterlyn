package co.sblock.Sblock.UserData;

/**
 * Represents each planet in the Medium.
 * 
 * @author FireNG
 * @author Jikoo
 */
public enum MediumPlanet {

	UNKNOWN, LOFAF, LOHAC, LOLAR, LOWAS;

	/**
	 * Gets the short name of a planet.
	 *
	 * @return the short name
	 */
	public String getShortName() {
		return this.name();
	}

	/**
	 * Gets the long name of a planet.
	 *
	 * @return the long name
	 */
	public String getLongName() {
		switch (this) {
		case LOFAF:
			return "Land of Frost and Frogs";
		case LOHAC:
			return "Land of Heat and Clockwork";
		case LOLAR:
			return "Land of Light and Rain";
		case LOWAS:
			return "Land of Wind and Shade";
		case UNKNOWN:
			return "Land of Fail and Downvotes";
		default:
			return "Land of Fail and Downvotes";
		}
	}

	/**
	 * Gets the planet.
	 *
	 * @param name the name of a planet
	 * @return the MediumPlanet
	 */
	public static MediumPlanet getPlanet(String name) {
		try {
			return MediumPlanet.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return MediumPlanet.UNKNOWN;
		}
	}
}
