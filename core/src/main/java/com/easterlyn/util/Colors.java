package com.easterlyn.util;

import com.easterlyn.EasterlynCore;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Colors {

  private static final Pattern COLOR_PATTERN = Pattern.compile("\\{color:([\\w\\.])+}");

  private static final Map<String, ChatColor> mappings = new HashMap<>();
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

  private Colors() {}

  public static void load(EasterlynCore plugin) {
    ConfigurationSection colorSection = plugin.getConfig().getConfigurationSection("colors");
    WEB_LINK = register(colorSection, "web_link", ChatColor.BLUE);
    COMMAND = register(colorSection, "command", ChatColor.AQUA);
    HIGHLIGHT = register(colorSection, "highlight", ChatColor.AQUA);
    CHANNEL = register(colorSection, "channel", ChatColor.GOLD);
    CHANNEL_OWNER = register(colorSection, "channel_owner", ChatColor.RED);
    CHANNEL_MODERATOR = register(colorSection, "channel_moderator", ChatColor.AQUA);
    CHANNEL_MEMBER = register(colorSection, "channel_member", ChatColor.GOLD);

    RANK_MEMBER = register(colorSection, "rank.member", ChatColor.DARK_PURPLE);
    RANK_RETIRED_STAFF = register(colorSection, "rank.retired_staff", ChatColor.DARK_PURPLE);
    RANK_STAFF = register(colorSection, "rank.staff", ChatColor.DARK_AQUA);
    RANK_MODERATOR = register(colorSection, "rank.moderator", ChatColor.DARK_AQUA);
    RANK_ADMIN = register(colorSection, "rank.admin", ChatColor.GOLD);
    RANK_HEAD_ADMIN = register(colorSection, "rank.head_admin", ChatColor.GOLD);

    // Standard colors used in language files
    register(colorSection, "normal.a", ChatColor.YELLOW);
    register(colorSection, "normal.b", ChatColor.DARK_AQUA);
    register(colorSection, "normal.c", ChatColor.GREEN);
    register(colorSection, "bad.a", ChatColor.RED);
    register(colorSection, "bad.b", ChatColor.GOLD);
    register(colorSection, "bad.c", ChatColor.DARK_AQUA);
    register(colorSection, "on", ChatColor.GREEN);
    register(colorSection, "off", ChatColor.RED);

    if (colorSection == null) {
      return;
    }

    // Custom defined colors
    for (String key : colorSection.getKeys(true)) {
      if (mappings.containsKey(key) || !colorSection.isString(key)) {
        continue;
      }
      register(colorSection, key, ChatColor.WHITE);
    }
  }

  public static ChatColor register(
      @Nullable ConfigurationSection colorSection,
      @NotNull String name,
      @NotNull ChatColor defaultColor) {
    ChatColor color = defaultColor;

    if (colorSection != null) {
      color = getOrDefault(colorSection.getString(name), defaultColor);
    }

    mappings.put(name.toLowerCase(Locale.ENGLISH), color);

    return color;
  }

  @Contract("_, !null -> !null")
  public static @Nullable ChatColor getOrDefault(
      @Nullable String colorName, @Nullable ChatColor defaultColor) {
    if (colorName == null) {
      return defaultColor;
    }

    colorName = colorName.toLowerCase(Locale.ENGLISH);
    ChatColor color = mappings.get(colorName);

    if (color != null) {
      return color;
    }

    try {
      return ChatColor.of(colorName);
    } catch (IllegalArgumentException e) {
      return defaultColor;
    }
  }

  public @NotNull static String addColor(@NotNull String string) {
    Matcher matcher = COLOR_PATTERN.matcher(string);
    StringBuffer builder = new StringBuffer();

    while (matcher.find()) {
      matcher.appendReplacement(
          builder, String.valueOf(getOrDefault(matcher.group(1), ChatColor.WHITE)));
    }
    matcher.appendTail(builder);

    return builder.toString();
  }
}
