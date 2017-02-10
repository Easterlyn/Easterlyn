package com.easterlyn.users;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents canon Classes, including those of mythological roles.
 * 
 * @author FireNG, Jikoo
 */
public class UserClass {

	private static final HashMap<String, UserClass> REGISTRY = new HashMap<>();

	static {
		registerDefault(new UserClass("Legend", 7));
		registerDefault(new UserClass("Captain", 7));
		registerDefault(new UserClass("Architect", 6));
		registerDefault(new UserClass("Sorcerer", 6));
		registerDefault(new UserClass("Oracle", 5));
		registerDefault(new UserClass("Dreamer", 5));
		registerDefault(new UserClass("Hoarder", 4));
		registerDefault(new UserClass("Hunter", 4));
		registerDefault(new UserClass("Protector", 3));
		registerDefault(new UserClass("Scavenger", 3));
		registerDefault(new UserClass("Spirit", 2));
		registerDefault(new UserClass("Scribe", 2));
		registerDefault(new UserClass("Priest", 1));
		registerDefault(new UserClass("Keeper", 1));
		registerDefault(new UserClass("Hobo", 0));
		registerDefault(new UserClass("Bucket", 0));

		registerDefault(new UserClass("Plebian", 0));
	}

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

	@Override
	public String toString() {
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
		String lowName = name.toLowerCase();
		if (REGISTRY.containsKey(lowName)) {
			return REGISTRY.get(lowName);
		}

		return new UserClass(name, 4);
	}

	/**
	 * Get a Collection of all registered UserClasses.
	 * 
	 * @return the UserClasses
	 */
	public static Collection<UserClass> values() {
		return REGISTRY.values();
	}

	private static void registerDefault(UserClass userClass) {
		REGISTRY.put(userClass.getDisplayName().toLowerCase(), userClass);
	}

}
