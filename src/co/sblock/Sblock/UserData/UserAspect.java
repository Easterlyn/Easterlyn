package co.sblock.Sblock.UserData;

/**
 * Represents each character aspect.
 * 
 * @author FireNG, Jikoo
 */
public enum UserAspect {
	BLOOD, BREATH, DOOM, HEART, HOPE, LIFE,
	LIGHT, MIND, RAGE, SPACE, TIME, VOID;

	/**
	 * Gets the display name of the PlayerAspect.
	 * 
	 * @return The display name of this PlayerAspect.
	 */
	public String getDisplayName() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}

	/**
	 * Gets the PlayerAspect.
	 * 
	 * @param name the name of an aspect
	 * 
	 * @return the PlayerAspect
	 */
	public static UserAspect getAspect(String name) {
		try {
			return UserAspect.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UserAspect.BREATH;
		}
	}
}
