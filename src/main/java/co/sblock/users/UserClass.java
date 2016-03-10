package co.sblock.users;

/**
 * Represents canon Classes, including those of mythological roles.
 * <p>
 * Mythological roles cannot be obtained by players without usage of admin commands.
 * 
 * @author FireNG, Jikoo
 */
public enum UserClass {
	BARD("Bard", 3),
	DOUCHE("Douche", 4),
	GENT("Gent", 3),
	HEIR("Heir", 3),
	KNIGHT("Knight", 5),
	LORD("Lord", 7),
	MAGE("Mage", 4),
	MAID("Maid", 4),
	MUSE("Muse", 0),
	ORACLE("Oracle", 4),
	PAGE("Page", 2),
	PRINCE("Prince", 6),
	ROGUE("Rogue", 2),
	SEER("Seer", 1),
	SYLPH("Sylph", 1),
	THIEF("Thief", 5),
	WASTE("Waste", 3),
	WITCH("Witch", 6),
	DEFAULT("Default", 4);

	private final String name;
	private final int activity;

	private UserClass(String name, int activity) {
		this.name = name;
		this.activity = activity;
	}

	/**
	 * Gets the display name of the UserClass.
	 * 
	 * @return the display name of this UserClass
	 */
	public String getDisplayName() {
		return this.name;
	}

	/**
	 * Gets the number of active abilities granted by this UserClass.
	 * 
	 * @return the number of abilities
	 */
	public int getActiveSkills() {
		return activity;
	}

	/**
	 * Gets the number of passive or reactive abilities granted by this UserClass.
	 * 
	 * @return the number of abilities
	 */
	public int getPassiveSkills() {
		return 7 - activity;
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
