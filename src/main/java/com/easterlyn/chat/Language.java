package com.easterlyn.chat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.easterlyn.Easterlyn;
import com.easterlyn.discord.Discord;
import com.easterlyn.module.Module;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;

/**
 * A module for language adjustments.
 * 
 * @author Jikoo
 */
public class Language extends Module {

	private static final Map<String, ChatColor> COLOR_DEF = new HashMap<>();

	static {
		for (ChatColor color : ChatColor.values()) {
			COLOR_DEF.put(color.getName().toLowerCase(), color);
		}
	}

	private final SimpleDateFormat TIME_24 = new SimpleDateFormat("HH:mm");
	private final Pattern specialContent = Pattern.compile("\\{([A-Z]+):(.*?)\\}");
	private final Map<String, String> translatedValues;

	private Discord discord;

	public Language(Easterlyn plugin) {
		super(plugin);
		this.translatedValues = new HashMap<>();

		InputStream stream = this.getPlugin().getResource("lang.yml");
		InputStreamReader reader = new InputStreamReader(stream);
		YamlConfiguration defaultStrings = YamlConfiguration.loadConfiguration(reader);
		YamlConfiguration config = this.getConfig();
		boolean changed = false;
		Set<String> paths = new HashSet<String>(defaultStrings.getKeys(true));
		for (Iterator<String> iterator = paths.iterator(); iterator.hasNext();) {
			String path = iterator.next();
			if (!defaultStrings.isString(path)) {
				iterator.remove();
				continue;
			}
			if (!config.isString(path)) {
				config.set(path, defaultStrings.get(path));
				changed = true;
			}
		}
		if (changed) {
			this.saveConfig();
		}
		ConfigurationSection colorSection = config.getConfigurationSection("color");
		for (String path : colorSection.getKeys(true)) {
			try {
				if (colorSection.isConfigurationSection(path)) {
					continue;
				}
				COLOR_DEF.put(path, ChatColor.valueOf(colorSection.getString(path)));
			} catch (IllegalArgumentException e) {
				getLogger().warning("Invalid chat color provided for path " + path);
			}
		}
		for (String path : paths) {
			this.loadValueFor(path);
		}
	}

	@Override
	protected void onEnable() {
		this.discord = this.getPlugin().getModule(Discord.class);
	}

	/**
	 * Gets a ChatColor by its definition.
	 * 
	 * @param path the definition of the color.
	 * 
	 * @return the ChatColor, or white if invalid
	 */
	public static ChatColor getColor(String path) {
		if (COLOR_DEF.containsKey(path)) {
			return COLOR_DEF.get(path);
		}
		System.err.println("[Easterlyn Lang] Invalid color requested, providing WHITE: " + path);
		return ChatColor.WHITE;
	}

	/**
	 * Gets a customizable String for the given path.
	 * 
	 * @param path the identifying path
	 * 
	 * @return the String represented or the given path if nonexistent
	 */
	public String getValue(String path) {
		return this.getValue(path, path, false);
	}

	/**
	 * Gets a customizable String for the given path.
	 * 
	 * @param path the identifying path
	 * @param defaultValue the default value to use if the path is not present
	 * 
	 * @return the String represented or the given path if nonexistent
	 */
	public String getValue(String path, String defaultValue) {
		return this.getValue(path, defaultValue, false);
	}

	/**
	 * Gets a customizable String for the given path with the option to do additional replacements
	 * for various variables.
	 * 
	 * @param path the identifying path
	 * @param addExtraValues true if additional replacement is to be done
	 * 
	 * @return the String represented or the given path if nonexistent
	 */
	public String getValue(String path, boolean addExtraValues) {
		return this.getValue(path, path, addExtraValues);
	}

	/**
	 * Gets a customizable String for the given path with the option to do additional replacements
	 * for various variables.
	 * 
	 * @param path the identifying path
	 * @param defaultValue the default value to use if the path is not present
	 * @param addExtraValues true if additional replacement is to be done
	 * 
	 * @return the String represented or the given path if nonexistent
	 */
	public String getValue(String path, String defaultValue, boolean addExtraValues) {
		String value = this.translatedValues.get(path);
		if (value == null) {
			value = defaultValue;
			if (defaultValue == path) {
				discord.postReport("Invalid lang path: " + path);
			}
		}
		if (addExtraValues) {
			return value.replace("{TIME}", TIME_24.format(new Date()));
		}
		return value;
	}

	private void loadValueFor(String path) {
		if (this.translatedValues.containsKey(path)) {
			return;
		}
		String content = this.getConfig().getString(path);
		Matcher matcher = this.specialContent.matcher(content);
		int lastIndex = 0;
		StringBuilder builder = new StringBuilder();
		while (matcher.find()) {
			builder.append(content.substring(lastIndex, matcher.start()));
			builder.append(this.getSpecialContent(matcher.group(1), matcher.group(2)));
			lastIndex = matcher.end();
		}
		builder.append(content.substring(lastIndex));
		this.translatedValues.put(path, builder.toString().replaceAll("\n+$", ""));
	}

	private String getSpecialContent(String type, String value) {
		String result;
		switch (type) {
		case "COLOR":
			result = getColor(value).toString();
			break;
		default:
			result = type + ":" + value;
			break;
		}
		return result;
	}

	@Override
	protected void onDisable() { }

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public String getName() {
		return "Lang";
	}

}
