package com.easterlyn.users;

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

	public static final UserAspect ADANSONIA = addDefault(new UserAspect("Adansonia", ChatColor.RED));

	public static final UserAspect ENERGY = addDefault(new UserAspect("Energy", ChatColor.GREEN));
	public static final UserAspect FROST = addDefault(new UserAspect("Frost", ChatColor.AQUA));
	public static final UserAspect KNOWLEDGE = addDefault(new UserAspect("Knowledge", ChatColor.DARK_RED));
	public static final UserAspect LIGHTNING = addDefault(new UserAspect("Lightning", ChatColor.YELLOW));
	public static final UserAspect LLAMAS = addDefault(new UserAspect("Llamas", ChatColor.DARK_GREEN));
	public static final UserAspect MEMES = addDefault(new UserAspect("Memes", ChatColor.LIGHT_PURPLE));
	public static final UserAspect MOONLIGHT = addDefault(new UserAspect("Moonlight", ChatColor.WHITE));
	public static final UserAspect RAIN = addDefault(new UserAspect("Rain", ChatColor.DARK_BLUE));
	public static final UserAspect ROYALTY = addDefault(new UserAspect("Royalty", ChatColor.DARK_PURPLE));
	public static final UserAspect SECRETS = addDefault(new UserAspect("Secrets", ChatColor.BLACK));
	public static final UserAspect SOULS = addDefault(new UserAspect("Souls", ChatColor.BLUE));
	public static final UserAspect STARS = addDefault(new UserAspect("Stars", ChatColor.GOLD));
	public static final UserAspect TIME = addDefault(new UserAspect("Time", ChatColor.DARK_AQUA));
	public static final UserAspect WIND = addDefault(new UserAspect("Wind", ChatColor.GRAY));
	public static final UserAspect VOID = addDefault(new UserAspect("Void", ChatColor.DARK_GRAY));

	public static final UserAspect EASTERLYN = addDefault(new UserAspect("Easterlyn", ChatColor.GREEN));

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
		String lowname = ChatColor.stripColor(name.toLowerCase());
		if (REGISTRY.containsKey(lowname)) {
			return REGISTRY.get(lowname);
		}
		return UserAspect.fromString(name);
	}

	private static UserAspect fromString(String aspect) {
		if (aspect.length() > 2 && aspect.charAt(0) == ChatColor.COLOR_CHAR) {
			return new UserAspect(aspect.substring(2), ChatColor.getByChar(aspect.charAt(1)));
		}
		return EASTERLYN;
	}

	private static UserAspect addDefault(UserAspect aspect) {
		REGISTRY.put(aspect.getDisplayName().toLowerCase(), aspect);
		return aspect;
	}

	public static Collection<UserAspect> values() {
		return REGISTRY.values();
	}

}
