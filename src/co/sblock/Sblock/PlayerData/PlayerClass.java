package co.sblock.Sblock.PlayerData;

/**
 * Represents each character class.
 * 
 * @author FireNG
 * @author Jikoo
 */
public enum PlayerClass {
	UNKNOWN, BARD, HEIR, KNIGHT, MAGE, MAID, PAGE, PRINCE, ROGUE, SEER, SYLPH, THEIF, WITCH;

	/**
	 * Gets the display name of the PlayerClass.
	 * 
	 * @return The display name of this PlayerClass.
	 */
	public String getDisplayName() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}

	/**
	 * Gets the class.
	 * 
	 * @param name the name of a PlayerClass
	 * @return the PlayerClass
	 */
	public static PlayerClass getClass(String name) {
		try {
			return PlayerClass.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return PlayerClass.UNKNOWN;
		}
	}
}
