package com.easterlyn.utilities.concurrent;

import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A thread-safe Configuration implementation.
 *
 * @author Jikoo
 */
public class ConcurrentConfiguration implements Configuration {

	private final ConfigurationSection internal;

	public ConcurrentConfiguration() {
		this.internal = new YamlConfiguration();
	}

	private ConcurrentConfiguration(ConfigurationSection configuration) {
		this.internal = configuration;
	}

	public static ConcurrentConfiguration load(File file) {
		return new ConcurrentConfiguration(YamlConfiguration.loadConfiguration(file));
	}


	public void save(File file) throws IOException {
		synchronized (internal) {
			if (internal instanceof FileConfiguration) {
				((FileConfiguration) internal).save(file);
			} else if (internal.getRoot() instanceof FileConfiguration) {
				((FileConfiguration) internal.getRoot()).save(file);
			} else {
				throw new UnsupportedOperationException(
						String.format("Cannot save internal ConfigurationSection implementation %s",
								internal.getClass().getName()));
			}
		}
	}

	@Override
	public Set<String> getKeys(boolean deep) {
		synchronized (internal) {
			return internal.getKeys(deep);
		}
	}

	@Override
	public Map<String, Object> getValues(boolean deep) {
		synchronized (internal) {
			return internal.getValues(deep);
		}
	}

	@Override
	public boolean contains(String path) {
		synchronized (internal) {
			return internal.contains(path);
		}
	}

	@Override
	public boolean contains(String path, boolean ignoreDefault) {
		synchronized (internal) {
			return internal.contains(path, ignoreDefault);
		}
	}

	@Override
	public boolean isSet(String path) {
		synchronized (internal) {
			return internal.isSet(path);
		}
	}

	@Override
	public String getCurrentPath() {
		synchronized (internal) {
			return internal.getCurrentPath();
		}
	}

	@Override
	public String getName() {
		synchronized (internal) {
			return internal.getName();
		}
	}

	@Override
	public Configuration getRoot() {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.getRoot());
		}
	}

	@Override
	public ConfigurationSection getParent() {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.getParent());
		}
	}

	@Override
	public Object get(String path) {
		synchronized (internal) {
			return internal.get(path);
		}
	}

	@Override
	public Object get(String path, Object defaultValue) {
		synchronized (internal) {
			return internal.get(path, defaultValue);
		}
	}

	@Override
	public void set(String path, Object value) {
		synchronized (internal) {
			internal.set(path, value);
		}
	}

	@Override
	public ConfigurationSection createSection(String path) {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.createSection(path));
		}
	}

	@Override
	public ConfigurationSection createSection(String path, Map<?, ?> mappings) {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.createSection(path, mappings));
		}
	}

	@Override
	public String getString(String path) {
		synchronized (internal) {
			return internal.getString(path);
		}
	}

	@Override
	public String getString(String path, String defaultValue) {
		synchronized (internal) {
			return internal.getString(path, defaultValue);
		}
	}

	@Override
	public boolean isString(String path) {
		synchronized (internal) {
			return internal.isString(path);
		}
	}

	@Override
	public int getInt(String path) {
		synchronized (internal) {
			return internal.getInt(path);
		}
	}

	@Override
	public int getInt(String path, int defaultValue) {
		synchronized (internal) {
			return internal.getInt(path, defaultValue);
		}
	}

	@Override
	public boolean isInt(String path) {
		synchronized (internal) {
			return internal.isInt(path);
		}
	}

	@Override
	public boolean getBoolean(String path) {
		synchronized (internal) {
			return internal.getBoolean(path);
		}
	}

	@Override
	public boolean getBoolean(String path, boolean defaultValue) {
		synchronized (internal) {
			return internal.getBoolean(path, defaultValue);
		}
	}

	@Override
	public boolean isBoolean(String path) {
		synchronized (internal) {
			return internal.isBoolean(path);
		}
	}

	@Override
	public double getDouble(String path) {
		synchronized (internal) {
			return internal.getDouble(path);
		}
	}

	@Override
	public double getDouble(String path, double defaultValue) {
		synchronized (internal) {
			return internal.getDouble(path, defaultValue);
		}
	}

	@Override
	public boolean isDouble(String path) {
		synchronized (internal) {
			return internal.isDouble(path);
		}
	}

	@Override
	public long getLong(String path) {
		synchronized (internal) {
			return internal.getLong(path);
		}
	}

	@Override
	public long getLong(String path, long defaultValue) {
		synchronized (internal) {
			return internal.getLong(path, defaultValue);
		}
	}

	@Override
	public boolean isLong(String path) {
		synchronized (internal) {
			return internal.isLong(path);
		}
	}

	@Override
	public List<?> getList(String path) {
		synchronized (internal) {
			return internal.getList(path);
		}
	}

	@Override
	public List<?> getList(String path, List<?> defaultValue) {
		synchronized (internal) {
			return internal.getList(path, defaultValue);
		}
	}

	@Override
	public boolean isList(String path) {
		synchronized (internal) {
			return internal.isList(path);
		}
	}

	@Override
	public List<String> getStringList(String path) {
		synchronized (internal) {
			return internal.getStringList(path);
		}
	}

	@Override
	public List<Integer> getIntegerList(String path) {
		synchronized (internal) {
			return internal.getIntegerList(path);
		}
	}

	@Override
	public List<Boolean> getBooleanList(String path) {
		synchronized (internal) {
			return internal.getBooleanList(path);
		}
	}

	@Override
	public List<Double> getDoubleList(String path) {
		synchronized (internal) {
			return internal.getDoubleList(path);
		}
	}

	@Override
	public List<Float> getFloatList(String path) {
		synchronized (internal) {
			return internal.getFloatList(path);
		}
	}

	@Override
	public List<Long> getLongList(String path) {
		synchronized (internal) {
			return internal.getLongList(path);
		}
	}

	@Override
	public List<Byte> getByteList(String path) {
		synchronized (internal) {
			return internal.getByteList(path);
		}
	}

	@Override
	public List<Character> getCharacterList(String path) {
		synchronized (internal) {
			return internal.getCharacterList(path);
		}
	}

	@Override
	public List<Short> getShortList(String path) {
		synchronized (internal) {
			return internal.getShortList(path);
		}
	}

	@Override
	public List<Map<?, ?>> getMapList(String path) {
		synchronized (internal) {
			return internal.getMapList(path);
		}
	}

	@Override
	public Vector getVector(String path) {
		synchronized (internal) {
			return internal.getVector(path);
		}
	}

	@Override
	public Vector getVector(String path, Vector defaultValue) {
		synchronized (internal) {
			return internal.getVector(path, defaultValue);
		}
	}

	@Override
	public boolean isVector(String path) {
		synchronized (internal) {
			return internal.isVector(path);
		}
	}

	@Override
	public OfflinePlayer getOfflinePlayer(String path) {
		synchronized (internal) {
			return internal.getOfflinePlayer(path);
		}
	}

	@Override
	public OfflinePlayer getOfflinePlayer(String path, OfflinePlayer defaultValue) {
		synchronized (internal) {
			return internal.getOfflinePlayer(path, defaultValue);
		}
	}

	@Override
	public boolean isOfflinePlayer(String path) {
		synchronized (internal) {
			return internal.isOfflinePlayer(path);
		}
	}

	@Override
	public ItemStack getItemStack(String path) {
		synchronized (internal) {
			return internal.getItemStack(path);
		}
	}

	@Override
	public ItemStack getItemStack(String path, ItemStack defaultValue) {
		synchronized (internal) {
			return internal.getItemStack(path, defaultValue);
		}
	}

	@Override
	public boolean isItemStack(String path) {
		synchronized (internal) {
			return internal.isItemStack(path);
		}
	}

	@Override
	public Color getColor(String path) {
		synchronized (internal) {
			return internal.getColor(path);
		}
	}

	@Override
	public Color getColor(String path, Color defaultValue) {
		synchronized (internal) {
			return internal.getColor(path, defaultValue);
		}
	}

	@Override
	public boolean isColor(String path) {
		synchronized (internal) {
			return internal.isColor(path);
		}
	}

	@Override
	public ConfigurationSection getConfigurationSection(String path) {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.getConfigurationSection(path));
		}
	}

	@Override
	public boolean isConfigurationSection(String path) {
		synchronized (internal) {
			return internal.isConfigurationSection(path);
		}
	}

	@Override
	public ConfigurationSection getDefaultSection() {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.getDefaultSection());
		}
	}

	@Override
	public void addDefault(String path, Object value) {
		synchronized (internal) {
			internal.addDefault(path, value);
		}
	}

	@Override
	public void addDefaults(Map<String, Object> defaults) {
		synchronized (internal) {
			internal.getRoot().addDefaults(defaults);
		}
	}

	@Override
	public void addDefaults(Configuration configuration) {
		synchronized (internal) {
			internal.getRoot().addDefaults(configuration);
		}
	}

	@Override
	public void setDefaults(Configuration configuration) {
		synchronized (internal) {
			internal.getRoot().setDefaults(configuration);
		}
	}

	@Override
	public Configuration getDefaults() {
		synchronized (internal) {
			return internal.getRoot().getDefaults();
		}
	}

	@Override
	public ConfigurationOptions options() {
		synchronized (internal) {
			return internal.getRoot().options();
		}
	}

}
