package co.sblock.users;

import org.bukkit.ChatColor;

import co.sblock.chat.ColorDef;

/**
 * Represents each character aspect.
 * 
 * @author FireNG, Jikoo, tmathmeyer
 */
public enum UserAspect {
	BLOOD("Blood", ColorDef.ASPECT_BLOOD),
	BREATH("Breath", ColorDef.ASPECT_BREATH),
	DOOM("Doom", ColorDef.ASPECT_DOOM),
	HEART("Heart", ColorDef.ASPECT_HEART),
	HOPE("Hope", ColorDef.ASPECT_HOPE),
	LIFE("Life", ColorDef.ASPECT_LIFE),
	LIGHT("Light", ColorDef.ASPECT_LIGHT),
	MIND("Mind", ColorDef.ASPECT_MIND),
	RAGE("Rage", ColorDef.ASPECT_RAGE),
	SPACE("Space", ColorDef.ASPECT_SPACE),
	TIME("Time", ColorDef.ASPECT_TIME),
	VOID("Void", ColorDef.ASPECT_VOID);

	/*
	 * final state variables
	 */
	private final String name;
	private final ChatColor chatColor;

	/**
	 * @param name the name of the aspect
	 * @param color the Color of the Aspect
	 */
	private UserAspect(String name, ChatColor color) {
		this.name = name;
		this.chatColor = color;
	}

	/**
	 * Gets the display name of the PlayerAspect.
	 * 
	 * @return The display name of this PlayerAspect.
	 */
	public String getDisplayName() {
		return this.name;
	}

	/**
	 * @return the color for this aspect
	 */
	public ChatColor getColor() {
		return this.chatColor;
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
