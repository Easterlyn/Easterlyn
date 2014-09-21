package co.sblock.users;

/**
 * Represents each character class.
 * 
 * @author FireNG, Jikoo
 */
public enum UserClass {
	BARD("Bard"),
	HEIR("Heir"),
	KNIGHT("Knight"),
	MAGE("Mage"),
	MAID("Maid"),
	PAGE("Page"),
	PRINCE("Prince"),
	ROGUE("Rogue"),
	SEER("Seer"),
	SYLPH("Sylph"),
	THIEF("Thief"),
	WITCH("Witch");

	private final String name;

	private UserClass(String name) {
		this.name = name;
	}

	/**
	 * Gets the display name of the UserClass.
	 * 
	 * @return The display name of this UserClass.
	 */
	public String getDisplayName() {
		return this.name;
	}

	/**
	 * Gets the UserClass.
	 * 
	 * @param name the name of a UserClass
	 * 
	 * @return the UserClass
	 */
	public static UserClass getClass(String name) {
		try {
			return UserClass.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UserClass.HEIR;
		}
	}
}
