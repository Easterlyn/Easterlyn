package co.sblock.users;

/**
 * Represents each planet in the Medium.
 * 
 * @author FireNG, Jikoo
 */
public enum MediumPlanet {

	LOFAF, LOHAC, LOLAR, LOWAS;

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
		switch (this) {
		case LOFAF:
			return "Land of Frost and Frogs";
		case LOHAC:
			return "Land of Heat and Clockwork";
		case LOLAR:
			return "Land of Light and Rain";
		case LOWAS:
			return "Land of Wind and Shade";
		default:
			return "Land of Fail and Downvotes";
		}
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
}
