package com.easterlyn.users;

import net.md_5.bungee.api.ChatColor;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents affinities for unlockable abilities.
 *
 * @author Jikoo, FireNG, tmathmeyer
 */
public class UserAffinity {

	private static final HashMap<String, UserAffinity> NAME_REGISTRY = new HashMap<>();
	private static final HashMap<ChatColor, UserAffinity> COLOR_REGISTRY = new HashMap<>();

	public static final UserAffinity EASTERLYN = addDefault(new UserAffinity("Easterlyn", ChatColor.WHITE));

	public static final UserAffinity LIGHT = addDefault(new UserAffinity("Light", ChatColor.YELLOW));
	public static final UserAffinity SHADOW = addDefault(new UserAffinity("Shadow", ChatColor.DARK_GRAY));
	public static final UserAffinity LIFE = addDefault(new UserAffinity("Life", ChatColor.DARK_GREEN));
	public static final UserAffinity DEATH = addDefault(new UserAffinity("Death", ChatColor.DARK_GRAY));
	public static final UserAffinity EARTH = addDefault(new UserAffinity("Earth", ChatColor.GRAY));
	public static final UserAffinity WATER = addDefault(new UserAffinity("Water", ChatColor.BLUE));
	public static final UserAffinity FIRE = addDefault(new UserAffinity("Fire", ChatColor.RED));
	public static final UserAffinity AIR = addDefault(new UserAffinity("Air", ChatColor.AQUA));
	public static final UserAffinity TIME = addDefault(new UserAffinity("Time", ChatColor.DARK_PURPLE));

	private final String name;
	private final ChatColor chatColor;

	/**
	 * @param name the name of the UserAffinity
	 * @param color the color of the UserAffinity
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

	public UserAffinity getAffinityGroup() {
		return COLOR_REGISTRY.getOrDefault(this.getColor(), EASTERLYN);
	}

	/**
	 * Gets the UserAffinity.
	 *
	 * @param name the name of an affinity
	 *
	 * @return the UserAffinity
	 */
	public static UserAffinity getAffinity(String name) {
		String lowname = ChatColor.stripColor(name.toLowerCase());
		if (NAME_REGISTRY.containsKey(lowname)) {
			return NAME_REGISTRY.get(lowname);
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
		NAME_REGISTRY.put(aspect.getDisplayName().toLowerCase(), aspect);
		COLOR_REGISTRY.put(aspect.getColor(), aspect);
		return aspect;
	}

	public static Collection<UserAffinity> values() {
		return NAME_REGISTRY.values();
	}

}
