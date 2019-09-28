package com.easterlyn.utilities.concurrent;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

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

	@NotNull
	@Override
	public Set<String> getKeys(boolean deep) {
		synchronized (internal) {
			return internal.getKeys(deep);
		}
	}

	@NotNull
	@Override
	public Map<String, Object> getValues(boolean deep) {
		synchronized (internal) {
			return internal.getValues(deep);
		}
	}

	@Override
	public boolean contains(@NotNull String path) {
		synchronized (internal) {
			return internal.contains(path);
		}
	}

	@Override
	public boolean contains(@NotNull String path, boolean ignoreDefault) {
		synchronized (internal) {
			return internal.contains(path, ignoreDefault);
		}
	}

	@Override
	public boolean isSet(@NotNull String path) {
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

	@NotNull
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
	public Object get(@NotNull String path) {
		synchronized (internal) {
			return internal.get(path);
		}
	}

	@Override
	public Object get(@NotNull String path, Object defaultValue) {
		synchronized (internal) {
			return internal.get(path, defaultValue);
		}
	}

	@Override
	public void set(@NotNull String path, Object value) {
		synchronized (internal) {
			internal.set(path, value);
		}
	}

	@NotNull
	@Override
	public ConfigurationSection createSection(@NotNull String path) {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.createSection(path));
		}
	}

	@NotNull
	@Override
	public ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> mappings) {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.createSection(path, mappings));
		}
	}

	@Override
	public String getString(@NotNull String path) {
		synchronized (internal) {
			return internal.getString(path);
		}
	}

	@Override
	public String getString(@NotNull String path, String defaultValue) {
		synchronized (internal) {
			return internal.getString(path, defaultValue);
		}
	}

	@Override
	public boolean isString(@NotNull String path) {
		synchronized (internal) {
			return internal.isString(path);
		}
	}

	@Override
	public int getInt(@NotNull String path) {
		synchronized (internal) {
			return internal.getInt(path);
		}
	}

	@Override
	public int getInt(@NotNull String path, int defaultValue) {
		synchronized (internal) {
			return internal.getInt(path, defaultValue);
		}
	}

	@Override
	public boolean isInt(@NotNull String path) {
		synchronized (internal) {
			return internal.isInt(path);
		}
	}

	@Override
	public boolean getBoolean(@NotNull String path) {
		synchronized (internal) {
			return internal.getBoolean(path);
		}
	}

	@Override
	public boolean getBoolean(@NotNull String path, boolean defaultValue) {
		synchronized (internal) {
			return internal.getBoolean(path, defaultValue);
		}
	}

	@Override
	public boolean isBoolean(@NotNull String path) {
		synchronized (internal) {
			return internal.isBoolean(path);
		}
	}

	@Override
	public double getDouble(@NotNull String path) {
		synchronized (internal) {
			return internal.getDouble(path);
		}
	}

	@Override
	public double getDouble(@NotNull String path, double defaultValue) {
		synchronized (internal) {
			return internal.getDouble(path, defaultValue);
		}
	}

	@Override
	public boolean isDouble(@NotNull String path) {
		synchronized (internal) {
			return internal.isDouble(path);
		}
	}

	@Override
	public long getLong(@NotNull String path) {
		synchronized (internal) {
			return internal.getLong(path);
		}
	}

	@Override
	public long getLong(@NotNull String path, long defaultValue) {
		synchronized (internal) {
			return internal.getLong(path, defaultValue);
		}
	}

	@Override
	public boolean isLong(@NotNull String path) {
		synchronized (internal) {
			return internal.isLong(path);
		}
	}

	@Override
	public List<?> getList(@NotNull String path) {
		synchronized (internal) {
			return internal.getList(path);
		}
	}

	@Override
	public List<?> getList(@NotNull String path, List<?> defaultValue) {
		synchronized (internal) {
			return internal.getList(path, defaultValue);
		}
	}

	@Override
	public boolean isList(@NotNull String path) {
		synchronized (internal) {
			return internal.isList(path);
		}
	}

	@NotNull
	@Override
	public List<String> getStringList(@NotNull String path) {
		synchronized (internal) {
			return internal.getStringList(path);
		}
	}

	@NotNull
	@Override
	public List<Integer> getIntegerList(@NotNull String path) {
		synchronized (internal) {
			return internal.getIntegerList(path);
		}
	}

	@NotNull
	@Override
	public List<Boolean> getBooleanList(@NotNull String path) {
		synchronized (internal) {
			return internal.getBooleanList(path);
		}
	}

	@NotNull
	@Override
	public List<Double> getDoubleList(@NotNull String path) {
		synchronized (internal) {
			return internal.getDoubleList(path);
		}
	}

	@NotNull
	@Override
	public List<Float> getFloatList(@NotNull String path) {
		synchronized (internal) {
			return internal.getFloatList(path);
		}
	}

	@NotNull
	@Override
	public List<Long> getLongList(@NotNull String path) {
		synchronized (internal) {
			return internal.getLongList(path);
		}
	}

	@NotNull
	@Override
	public List<Byte> getByteList(@NotNull String path) {
		synchronized (internal) {
			return internal.getByteList(path);
		}
	}

	@NotNull
	@Override
	public List<Character> getCharacterList(@NotNull String path) {
		synchronized (internal) {
			return internal.getCharacterList(path);
		}
	}

	@NotNull
	@Override
	public List<Short> getShortList(@NotNull String path) {
		synchronized (internal) {
			return internal.getShortList(path);
		}
	}

	@NotNull
	@Override
	public List<Map<?, ?>> getMapList(@NotNull String path) {
		synchronized (internal) {
			return internal.getMapList(path);
		}
	}

	@Override
	public <T> T getObject(@NotNull String s, @NotNull Class<T> aClass) {
		synchronized (internal) {
			return internal.getObject(s, aClass);
		}
	}

	@Override
	public <T> T getObject(@NotNull String s, @NotNull Class<T> aClass, T t) {
		synchronized (internal) {
			return internal.getObject(s, aClass, t);
		}
	}

	@Override
	public <T extends ConfigurationSerializable> T getSerializable(@NotNull String s, @NotNull Class<T> aClass) {
		synchronized (internal) {
			return internal.getSerializable(s, aClass);
		}
	}

	@Override
	public <T extends ConfigurationSerializable> T getSerializable(@NotNull String s, @NotNull Class<T> aClass, T t) {
		synchronized (internal) {
			return internal.getSerializable(s, aClass, t);
		}
	}

	@Override
	public Vector getVector(@NotNull String path) {
		synchronized (internal) {
			return internal.getVector(path);
		}
	}

	@Override
	public Vector getVector(@NotNull String path, Vector defaultValue) {
		synchronized (internal) {
			return internal.getVector(path, defaultValue);
		}
	}

	@Override
	public boolean isVector(@NotNull String path) {
		synchronized (internal) {
			return internal.isVector(path);
		}
	}

	@Override
	public OfflinePlayer getOfflinePlayer(@NotNull String path) {
		synchronized (internal) {
			return internal.getOfflinePlayer(path);
		}
	}

	@Override
	public OfflinePlayer getOfflinePlayer(@NotNull String path, OfflinePlayer defaultValue) {
		synchronized (internal) {
			return internal.getOfflinePlayer(path, defaultValue);
		}
	}

	@Override
	public boolean isOfflinePlayer(@NotNull String path) {
		synchronized (internal) {
			return internal.isOfflinePlayer(path);
		}
	}

	@Override
	public ItemStack getItemStack(@NotNull String path) {
		synchronized (internal) {
			return internal.getItemStack(path);
		}
	}

	@Override
	public ItemStack getItemStack(@NotNull String path, ItemStack defaultValue) {
		synchronized (internal) {
			return internal.getItemStack(path, defaultValue);
		}
	}

	@Override
	public boolean isItemStack(@NotNull String path) {
		synchronized (internal) {
			return internal.isItemStack(path);
		}
	}

	@Override
	public Color getColor(@NotNull String path) {
		synchronized (internal) {
			return internal.getColor(path);
		}
	}

	@Override
	public Color getColor(@NotNull String path, Color defaultValue) {
		synchronized (internal) {
			return internal.getColor(path, defaultValue);
		}
	}

	@Override
	public boolean isColor(@NotNull String path) {
		synchronized (internal) {
			return internal.isColor(path);
		}
	}

	@Nullable
	@Override
	public Location getLocation(@NotNull String path) {
		synchronized (internal) {
			return internal.getLocation(path);
		}
	}

	@Nullable
	@Override
	public Location getLocation(@NotNull String path, @Nullable Location location) {
		synchronized (internal) {
			return internal.getLocation(path, location);
		}
	}

	@Override
	public boolean isLocation(@NotNull String path) {
		synchronized (internal) {
			return internal.isLocation(path);
		}
	}

	@Override
	public ConfigurationSection getConfigurationSection(@NotNull String path) {
		synchronized (internal) {
			return new ConcurrentConfiguration(internal.getConfigurationSection(path));
		}
	}

	@Override
	public boolean isConfigurationSection(@NotNull String path) {
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
	public void addDefault(@NotNull String path, Object value) {
		synchronized (internal) {
			internal.addDefault(path, value);
		}
	}

	@Override
	public void addDefaults(@NotNull Map<String, Object> defaults) {
		synchronized (internal) {
			defaults.forEach(internal::addDefault);
		}
	}

	@Override
	public void addDefaults(@NotNull Configuration configuration) {
		synchronized (internal) {
			configuration.getKeys(false).forEach(key -> internal.addDefault(key, configuration.get(key)));
		}
	}

	@Override
	public void setDefaults(@NotNull Configuration configuration) {
		synchronized (internal) {
			ConfigurationSection defaultSection = internal.getDefaultSection();
			if (defaultSection != null){
				new HashSet<>(internal.getDefaultSection().getKeys(false)).forEach(key -> internal.addDefault(key, null));
			}

			configuration.getKeys(false).forEach(key -> internal.addDefault(key, configuration.get(key)));
		}
	}

	@Override
	public Configuration getDefaults() {
		synchronized (internal) {
			return internal.getRoot().getDefaults();
		}
	}

	@NotNull
	@Override
	public ConfigurationOptions options() {
		synchronized (internal) {
			return internal.getRoot().options();
		}
	}

}
