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
		REGISTRY.put("bard", new UserClass("Bard", 3));
		REGISTRY.put("douche", new UserClass("Douche", 4));
		REGISTRY.put("gent", new UserClass("Gent", 3));
		REGISTRY.put("heir", new UserClass("Heir", 3));
		REGISTRY.put("knight", new UserClass("Knight", 5));
		REGISTRY.put("lord", new UserClass("Lord", 7));
		REGISTRY.put("mage", new UserClass("Mage", 4));
		REGISTRY.put("maid", new UserClass("Maid", 4));
		REGISTRY.put("muse", new UserClass("Muse", 0));
		REGISTRY.put("nerd", new UserClass("Nerd", 0));
		REGISTRY.put("page", new UserClass("Page", 2));
		REGISTRY.put("prince", new UserClass("Prince", 6));
		REGISTRY.put("rogue", new UserClass("Rogue", 2));
		REGISTRY.put("seer", new UserClass("Seer", 1));
		REGISTRY.put("sylph", new UserClass("Sylph", 1));
		REGISTRY.put("thief", new UserClass("Thief", 5));
		REGISTRY.put("waste", new UserClass("Waste", 3));
		REGISTRY.put("witch", new UserClass("Witch", 6));
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

}
