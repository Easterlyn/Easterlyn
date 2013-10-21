package co.sblock.Sblock.UserData;

/**
 * Represents a dream planet.
 * 
 * @author FireNG, Jikoo
 */
public enum DreamPlanet {

	NONE("Earth"), PROSPIT("InnerCircle"), DERSE("OuterCircle");

	/** The name of the <code>World</code>. */
	private String worldName;

	/**
	 * Constructor for <code>DreamPlanet</code>.
	 * 
	 * @param worldName
	 *            the name of the <code>World</code> used for this
	 *            <code>DreamPlanet</code>.
	 */
	DreamPlanet(String worldName) {
		this.worldName = worldName;
	}

	/**
	 * Gets the <code>DreamPlanet</code>'s display name.
	 * 
	 * 
	 * @return The display name of this <code>DreamPlanet</code>.
	 */
	public String getDisplayName() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}

	/**
	 * Gets the name of the <code>World</code>.
	 * 
	 * 
	 * @return the <code>World</code> name
	 */
	public String getWorldName() {
		return this.worldName;
	}

	/**
	 * Gets a <code>DreamPlanet</code> by name.
	 * 
	 * @param name
	 *            the name of a <code>DreamPlanet</code>
	 * 
	 * @return the <code>DreamPlanet</code> specified,
	 *         <code>DreamPlanet.NONE</code> if invalid.
	 */
	public static DreamPlanet getPlanet(String name) {
		try {
			return DreamPlanet.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return DreamPlanet.NONE;
		}
	}
}
