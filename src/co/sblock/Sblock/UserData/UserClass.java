package co.sblock.Sblock.UserData;

/**
 * Represents each character class.
 * 
 * @author FireNG
 * @author Jikoo
 */
public enum UserClass {
	UNKNOWN, BARD, HEIR, KNIGHT, MAGE, MAID, PAGE, PRINCE, ROGUE, SEER, SYLPH, THEIF, WITCH;

	/**
	 * Gets the display name of the UserClass.
	 * 
	 * @return The display name of this UserClass.
	 */
	public String getDisplayName() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}

	/**
	 * Gets the class.
	 * 
	 * @param name the name of a UserClass
	 * @return the UserClass
	 */
	public static UserClass getClass(String name) {
		try {
			return UserClass.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UserClass.UNKNOWN;
		}
	}
}
