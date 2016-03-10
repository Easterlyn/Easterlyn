package co.sblock.users;

import net.md_5.bungee.api.ChatColor;

/**
 * Represents canon Aspects, including those of mythological roles.
 * <p>
 * Mythological roles cannot be obtained by players without usage of admin commands.
 * 
 * @author FireNG, Jikoo, tmathmeyer
 */
public enum UserAspect {
	BLOOD("Blood", ChatColor.DARK_RED),
	BREATH("Breath", ChatColor.AQUA),
	DOOM("Doom", ChatColor.DARK_GREEN),
	HEART("Heart", ChatColor.LIGHT_PURPLE),
	HOPE("Hope", ChatColor.GOLD),
	LIFE("Life", ChatColor.GREEN),
	LIGHT("Light", ChatColor.YELLOW),
	LIPS("Lips", ChatColor.LIGHT_PURPLE),
	MIND("Mind", ChatColor.DARK_AQUA),
	PISS("Piss", ChatColor.YELLOW),
	RAGE("Rage", ChatColor.DARK_PURPLE),
	SPACE("Space", ChatColor.DARK_GRAY),
	TEARS("Tears", ChatColor.AQUA),
	TIME("Time", ChatColor.RED),
	VOID("Void", ChatColor.DARK_BLUE),
	DEFAULT("Default", ChatColor.WHITE);

	private final String name;
	private final ChatColor chatColor;

	/**
	 * @param name the name of the Aspect
	 * @param color the color of the Aspect
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
