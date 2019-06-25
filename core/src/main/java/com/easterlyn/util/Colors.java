package com.easterlyn.util;

import com.easterlyn.Easterlyn;
import java.util.Locale;
import net.md_5.bungee.api.ChatColor;

public class Colors {

	// TODO should this be a Pair, etc. that gets provided by some form of Supplier/Function per-plugin per-path?
	// At that rate it may be better to have separate color providers per-module
	public static ChatColor WEB_LINK = ChatColor.BLUE;
	public static ChatColor COMMAND = ChatColor.AQUA;
	public static ChatColor HIGHLIGHT = ChatColor.AQUA;
	public static ChatColor CHANNEL = ChatColor.GOLD;
	public static ChatColor CHANNEL_OWNER = ChatColor.RED;
	public static ChatColor CHANNEL_MODERATOR = ChatColor.AQUA;
	public static ChatColor CHANNEL_MEMBER = ChatColor.GOLD;

	public static ChatColor RANK_MEMBER = ChatColor.DARK_PURPLE;
	public static ChatColor RANK_RETIRED_STAFF = ChatColor.DARK_PURPLE;
	public static ChatColor RANK_STAFF = ChatColor.DARK_AQUA;
	public static ChatColor RANK_MODERATOR = ChatColor.DARK_AQUA;
	public static ChatColor RANK_ADMIN = ChatColor.GOLD;
	public static ChatColor RANK_HEAD_ADMIN = ChatColor.GOLD;

	public static void load(Easterlyn plugin) {
		WEB_LINK = getOrDefault(plugin.getConfig().getString("colors.web_link"), ChatColor.BLUE);
		COMMAND = getOrDefault(plugin.getConfig().getString("colors.command"), ChatColor.AQUA);
		HIGHLIGHT = getOrDefault(plugin.getConfig().getString("colors.highlight"), ChatColor.AQUA);
		CHANNEL = getOrDefault(plugin.getConfig().getString("colors.channel"), ChatColor.GOLD);
		CHANNEL_OWNER = getOrDefault(plugin.getConfig().getString("colors.channel_owner"), ChatColor.RED);
		CHANNEL_MODERATOR = getOrDefault(plugin.getConfig().getString("colors.channel_moderator"), ChatColor.AQUA);
		CHANNEL_MEMBER = getOrDefault(plugin.getConfig().getString("colors.channel_member"), ChatColor.GOLD);

		RANK_MEMBER = getOrDefault(plugin.getConfig().getString("colors.rank.member"), ChatColor.DARK_PURPLE);
		RANK_RETIRED_STAFF = getOrDefault(plugin.getConfig().getString("colors.rank.retired_staff"), ChatColor.DARK_PURPLE);
		RANK_STAFF = getOrDefault(plugin.getConfig().getString("colors.rank.staff"), ChatColor.DARK_AQUA);
		RANK_MODERATOR = getOrDefault(plugin.getConfig().getString("colors.rank.moderator"), ChatColor.DARK_AQUA);
		RANK_ADMIN = getOrDefault(plugin.getConfig().getString("colors.rank.admin"), ChatColor.GOLD);
		RANK_HEAD_ADMIN = getOrDefault(plugin.getConfig().getString("colors.rank.head_admin"), ChatColor.GOLD);
	}

	public static ChatColor getOrDefault(String colorName, ChatColor defaultColor) {
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
