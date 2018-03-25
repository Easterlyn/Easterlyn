package com.easterlyn.users;

import net.md_5.bungee.api.ChatColor;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents canon Aspects, including those of mythological roles.
 * <p>
 * Mythological roles cannot be obtained by players without usage of admin commands.
 *
 * @author FireNG, Jikoo, tmathmeyer
 */
public class UserAffinity {

	private static final HashMap<String, UserAffinity> REGISTRY = new HashMap<>();

	public static final UserAffinity EASTERLYN = addDefault(new UserAffinity("Easterlyn", ChatColor.WHITE));

	public static final UserAffinity LIGHT = addDefault(new UserAffinity("Light", ChatColor.GOLD));
	public static final UserAffinity SHADOW = addDefault(new UserAffinity("Shadow", ChatColor.DARK_GRAY));
	public static final UserAffinity LIFE = addDefault(new UserAffinity("Life", ChatColor.GREEN));
	public static final UserAffinity DEATH = addDefault(new UserAffinity("Death", ChatColor.DARK_RED));
	public static final UserAffinity EARTH = addDefault(new UserAffinity("Earth", ChatColor.DARK_GREEN));
	public static final UserAffinity WATER = addDefault(new UserAffinity("Water", ChatColor.BLUE));
	public static final UserAffinity FIRE = addDefault(new UserAffinity("Fire", ChatColor.RED));
	public static final UserAffinity AIR = addDefault(new UserAffinity("Air", ChatColor.YELLOW));
	public static final UserAffinity TIME = addDefault(new UserAffinity("Time", ChatColor.DARK_PURPLE));
	public static final UserAffinity SECRETS = addDefault(new UserAffinity("Secrets", ChatColor.DARK_AQUA));

	private final String name;
	private final ChatColor chatColor;

	/**
	 * @param name the name of the Aspect
	 * @param color the color of the Aspect
	 */
	private UserAffinity(String name, ChatColor color) {
		this.name = name;
		this.chatColor = color;
	}

	/**
	 * Gets the display name of the UserAffinity.
	 *
	 * @return The display name of this UserAffinity.
	 */
	public String getDisplayName() {
		return this.name;
	}

	/**
	 * @return the color for this UserAffinity
	 */
	public ChatColor getColor() {
		return this.chatColor;
	}

	/*
	 * Returns a String representation of this UserAffinity.
	 */
	@Override
	public String toString() {
		return this.chatColor + this.name;
	}

	/**
	 * Gets the UserAffinity.
	 *
	 * @param name the name of an aspect
	 *
	 * @return the UserAffinity
	 */
	public static UserAffinity getAffinity(String name) {
		String lowname = ChatColor.stripColor(name.toLowerCase());
		if (REGISTRY.containsKey(lowname)) {
			return REGISTRY.get(lowname);
		}
		return UserAffinity.fromString(name);
	}

	private static UserAffinity fromString(String aspect) {
		if (aspect.length() > 2 && aspect.charAt(0) == ChatColor.COLOR_CHAR) {
			return new UserAffinity(aspect.substring(2), ChatColor.getByChar(aspect.charAt(1)));
		}
		return EASTERLYN;
	}

	private static UserAffinity addDefault(UserAffinity aspect) {
		REGISTRY.put(aspect.getDisplayName().toLowerCase(), aspect);
		return aspect;
	}

	public static Collection<UserAffinity> values() {
		return REGISTRY.values();
	}

}
