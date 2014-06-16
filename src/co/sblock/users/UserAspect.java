package co.sblock.users;

import org.bukkit.ChatColor;

import co.sblock.chat.ColorDef;

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

	public ChatColor getColor() {
		switch (this) {
		case BLOOD:
			return ColorDef.ASPECT_BLOOD;
		case BREATH:
			return ColorDef.ASPECT_BREATH;
		case DOOM:
			return ColorDef.ASPECT_DOOM;
		case HEART:
			return ColorDef.ASPECT_HEART;
		case HOPE:
			return ColorDef.ASPECT_HOPE;
		case LIFE:
			return ColorDef.ASPECT_LIFE;
		case LIGHT:
			return ColorDef.ASPECT_LIGHT;
		case MIND:
			return ColorDef.ASPECT_MIND;
		case RAGE:
			return ColorDef.ASPECT_RAGE;
		case SPACE:
			return ColorDef.ASPECT_SPACE;
		case TIME:
			return ColorDef.ASPECT_TIME;
		case VOID:
			return ColorDef.ASPECT_VOID;
		default:
			return ColorDef.RANK_HERO;
		}
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
