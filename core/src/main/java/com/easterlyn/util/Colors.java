package com.easterlyn.util;

import com.easterlyn.Easterlyn;
import java.util.Locale;
import net.md_5.bungee.api.ChatColor;

public class Colors {

	public static ChatColor WEB_LINK = ChatColor.BLUE;
	public static ChatColor CHANNEL = ChatColor.GOLD;
	public static ChatColor COMMAND = ChatColor.AQUA;

	public static void load(Easterlyn plugin) {
		WEB_LINK = getOrDefault(plugin.getConfig().getString("colors.web_link"), ChatColor.BLUE);
		CHANNEL = getOrDefault(plugin.getConfig().getString("colors.channel"), ChatColor.GOLD);
		COMMAND = getOrDefault(plugin.getConfig().getString("colors.command"), ChatColor.AQUA);
	}

	private static ChatColor getOrDefault(String colorName, ChatColor defaultColor) {
		if (colorName == null) {
			return defaultColor;
		}
		try {
			return ChatColor.valueOf(colorName.toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException e) {
			return defaultColor;
		}
	}

	private Colors() {}

}
