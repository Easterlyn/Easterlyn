package co.sblock.Sblock.PlayerData;

/**
 * Represents each character aspect.
 * 
 * @author FireNG
 * @author Jikoo
 */
public enum PlayerAspect {
	UNKNOWN, BLOOD, BREATH, DOOM, HEART, HOPE, LIFE, LIGHT, MIND, RAGE, SPACE, TIME, VOID;

	/**
	 * Gets the display name of the aspect.
	 * 
	 * @return The display name of this aspect.
	 */
	public String getDisplayName() {
		return this.name().charAt(0) + this.name().substring(1).toLowerCase();
	}

	/**
	 * Gets the aspect.
	 *
	 * @param name the name of an aspect
	 * @return the PlayerAspect
	 */
	public static PlayerAspect getAspect(String name) {
		try {
			return PlayerAspect.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return PlayerAspect.UNKNOWN;
		}
	}
}
