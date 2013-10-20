package co.sblock.Sblock.UserData;

/**
 * Represents each character class.
 * 
 * @author FireNG, Jikoo
 */
public enum UserClass {
	NONE, BARD, HEIR, KNIGHT, MAGE, MAID, PAGE,
	PRINCE, ROGUE, SEER, SYLPH, THEIF, WITCH;

	/**
	 * Gets the display name of the <code>UserClass</code>.
	 * 
	 * @return The display name of this <code>UserClass</code>.
	 */
	public String getDisplayName() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}

	/**
	 * Gets the <code>UserClass</code>.
	 * 
	 * @param name
	 *            the name of a <code>UserClass</code>
	 * 
	 * @return the <code>UserClass</code>
	 */
	public static UserClass getClass(String name) {
		try {
			return UserClass.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UserClass.NONE;
		}
	}
}
