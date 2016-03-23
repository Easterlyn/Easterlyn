package co.sblock.chat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import co.sblock.Sblock;
import co.sblock.module.Module;

import net.md_5.bungee.api.ChatColor;

/**
 * A module for language adjustments.
 * 
 * @author Jikoo
 */
public class Language extends Module {

	private static final Map<String, ChatColor> COLOR_DEF = new HashMap<>();

	private final SimpleDateFormat TIME_24 = new SimpleDateFormat("HH:mm");
	private final Pattern specialContent = Pattern.compile("\\{([A-Z]+):(.*?)\\}");
	private final Map<String, String> translatedValues;

	public Language(Sblock plugin) {
		super(plugin);
		this.translatedValues = new HashMap<>();

		InputStream stream = this.getPlugin().getResource("lang.yml");
		InputStreamReader reader = new InputStreamReader(stream);
		YamlConfiguration defaultStrings = YamlConfiguration.loadConfiguration(reader);
		YamlConfiguration config = this.getConfig();
		boolean changed = false;
		Set<String> paths = defaultStrings.getKeys(true);
		for (String path : paths) {
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
	protected void onEnable() { }

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
		System.err.println("[Sblock Lang] Invalid color requested, providing WHITE: " + path);
		return ChatColor.WHITE;
	}

	/**
	 * Gets a customizable String for the given path.
	 * 
	 * @param path the identifying path
	 * 
	 * @return the String, or a String "null" if nonexistant.
	 */
	public String getValue(String path) {
		return this.getValue(path, false);
	}

	/**
	 * Gets a customizable String for the given path with the option to do additional replacements
	 * for various variables.
	 * 
	 * @param path the identifying path
	 * @param addExtraValues true if additional replacement is to be done
	 * 
	 * @return the String, or a String "null" if nonexistant.
	 */
	public String getValue(String path, boolean addExtraValues) {
		String value = this.translatedValues.get(path);
		if (value == null) {
			value = path;
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
		YamlConfiguration config = this.getConfig();
		String content = config.getString(path);
		Matcher matcher = specialContent.matcher(content);
		while (matcher.find()) {
			content = matcher.replaceFirst(this.getSpecialContent(matcher.group(1), matcher.group(2)));
		}
		content = content.replace("\\n", "\n");
		this.translatedValues.put(path, content);
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
