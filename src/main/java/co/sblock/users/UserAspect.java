package co.sblock.users;

import java.util.Collection;
import java.util.HashMap;

import net.md_5.bungee.api.ChatColor;

/**
 * Represents canon Aspects, including those of mythological roles.
 * <p>
 * Mythological roles cannot be obtained by players without usage of admin commands.
 * 
 * @author FireNG, Jikoo, tmathmeyer
 */
public class UserAspect {

	private static final HashMap<String, UserAspect> REGISTRY = new HashMap<>();

	public static final UserAspect BLOOD = addDefault("blood", new UserAspect("Blood", ChatColor.DARK_RED));
	public static final UserAspect BREATH = addDefault("breath", new UserAspect("Breath", ChatColor.AQUA));
	public static final UserAspect DOOM = addDefault("doom", new UserAspect("Doom", ChatColor.DARK_GREEN));
	public static final UserAspect HEART = addDefault("heart", new UserAspect("Heart", ChatColor.LIGHT_PURPLE));
	public static final UserAspect HOPE = addDefault("hope", new UserAspect("Hope", ChatColor.GOLD));
	public static final UserAspect LIFE = addDefault("life", new UserAspect("Life", ChatColor.GREEN));
	public static final UserAspect LIGHT = addDefault("light", new UserAspect("Light", ChatColor.YELLOW));
	public static final UserAspect LIPS = addDefault("lips", new UserAspect("Lips", ChatColor.LIGHT_PURPLE));
	public static final UserAspect MIND = addDefault("mind", new UserAspect("Mind", ChatColor.DARK_AQUA));
	public static final UserAspect PISS = addDefault("piss", new UserAspect("Piss", ChatColor.YELLOW));
	public static final UserAspect RAGE = addDefault("rage", new UserAspect("Rage", ChatColor.DARK_PURPLE));
	public static final UserAspect SPACE = addDefault("space", new UserAspect("Space", ChatColor.DARK_GRAY));
	public static final UserAspect TEARS = addDefault("tears", new UserAspect("Tears", ChatColor.AQUA));
	public static final UserAspect TIME = addDefault("time", new UserAspect("Time", ChatColor.RED));
	public static final UserAspect VOID = addDefault("void", new UserAspect("Void", ChatColor.DARK_BLUE));

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
	 * Gets the display name of the UserAspect.
	 * 
	 * @return The display name of this UserAspect.
	 */
	public String getDisplayName() {
		return this.name;
	}

	/**
	 * @return the color for this UserAspect
	 */
	public ChatColor getColor() {
		return this.chatColor;
	}

	/*
	 * Returns a String representation of this UserAspect.
	 */
	@Override
	public String toString() {
		return this.chatColor + this.name;
	}

	/**
	 * Gets the UserAspect.
	 * 
	 * @param name the name of an aspect
	 * 
	 * @return the UserAspect
	 */
	public static UserAspect getAspect(String name) {
		String lowname = name.toLowerCase();
		if (REGISTRY.containsKey(lowname)) {
			return REGISTRY.get(lowname);
		}
		return UserAspect.fromString(name);
	}

	private static UserAspect fromString(String aspect) {
		if (aspect.length() > 2 && aspect.charAt(0) == ChatColor.COLOR_CHAR) {
			return new UserAspect(aspect.substring(2), ChatColor.getByChar(aspect.charAt(1)));
		}
		return BREATH;
	}

	private static UserAspect addDefault(String ID, UserAspect aspect) {
		REGISTRY.put(ID, aspect);
		return aspect;
	}

	public static Collection<UserAspect> values() {
		return REGISTRY.values();
	}
}
