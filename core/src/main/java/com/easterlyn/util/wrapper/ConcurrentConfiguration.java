package com.easterlyn.util.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A thread-safe Configuration implementation.
 *
 * @author Jikoo
 */
public class ConcurrentConfiguration implements Configuration {

  private final Plugin plugin;
  private final @Nullable File file;
  private final Object lock;
  private final ConfigurationSection internal;
  private boolean dirty = false;
  private BukkitTask saveTask;

  public ConcurrentConfiguration(Plugin plugin) {
    this(plugin, null, new Object(), new YamlConfiguration());
  }

  private ConcurrentConfiguration(
      Plugin plugin, @Nullable File file, Object lock, ConfigurationSection section) {
    this.plugin = plugin;
    this.file = file;
    this.lock = lock;
    this.internal = section;
  }

  public static ConcurrentConfiguration load(Plugin plugin, File file) {
    return new ConcurrentConfiguration(
        plugin,
        file,
        new Object(),
        file != null ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration());
  }

  public void save(File file) throws IOException {
    if (this.file != null) {
      throw new IllegalStateException("ConcurrentConfiguration is set to autosave!");
    }
    saveNow(file);
  }

  private void save() {
    if (file == null || saveTask != null || !dirty) {
      return;
    }
    try {
      saveTask =
          new BukkitRunnable() {
            @Override
            public void run() {
              try {
                saveNow(file);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
              super.cancel();
              try {
                saveNow(file);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }.runTaskLater(plugin, 200L);
    } catch (IllegalStateException e) {
      // Plugin is being disabled, cannot schedule tasks
      try {
        saveNow(file);
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }
  }

  private void saveNow(File file) throws IOException {
    synchronized (lock) {
      if (!this.dirty) {
        return;
      }

      if (internal.getRoot() instanceof FileConfiguration) {
        ((FileConfiguration) internal.getRoot()).save(file);
      } else {
        throw new UnsupportedOperationException(
            String.format(
                "Cannot save internal ConfigurationSection implementation %s",
                internal.getClass().getName()));
      }
      this.dirty = false;
    }
  }

  @Override
  public @NotNull Set<String> getKeys(boolean deep) {
    synchronized (lock) {
      return internal.getKeys(deep);
    }
  }

  @Override
  public @NotNull Map<String, Object> getValues(boolean deep) {
    synchronized (lock) {
      return internal.getValues(deep);
    }
  }

  @Override
  public boolean contains(@NotNull String path) {
    synchronized (lock) {
      return internal.contains(path);
    }
  }

  @Override
  public boolean contains(@NotNull String path, boolean ignoreDefault) {
    synchronized (lock) {
      return internal.contains(path, ignoreDefault);
    }
  }

  @Override
  public boolean isSet(@NotNull String path) {
    synchronized (lock) {
      return internal.isSet(path);
    }
  }

  @Override
  public String getCurrentPath() {
    synchronized (lock) {
      return internal.getCurrentPath();
    }
  }

  @Override
  public @NotNull String getName() {
    synchronized (lock) {
      return internal.getName();
    }
  }

  @Override
  public Configuration getRoot() {
    synchronized (lock) {
      return new ConcurrentConfiguration(plugin, file, lock, internal.getRoot());
    }
  }

  @Override
  public ConfigurationSection getParent() {
    synchronized (lock) {
      return new ConcurrentConfiguration(plugin, file, lock, internal.getParent());
    }
  }

  @Override
  public Object get(@NotNull String path) {
    synchronized (lock) {
      return internal.get(path);
    }
  }

  @Override
  public Object get(@NotNull String path, Object defaultValue) {
    synchronized (lock) {
      return internal.get(path, defaultValue);
    }
  }

  @Override
  public void set(@NotNull String path, @Nullable Object value) {
    synchronized (lock) {
      internal.set(path, value);
      dirty = true;
      save();
    }
  }

  @Override
  public @NotNull ConfigurationSection createSection(@NotNull String path) {
    synchronized (lock) {
      return new ConcurrentConfiguration(plugin, file, lock, internal.createSection(path));
    }
  }

  @Override
  public @NotNull ConfigurationSection createSection(
      @NotNull String path, @NotNull Map<?, ?> mappings) {
    synchronized (lock) {
      return new ConcurrentConfiguration(
          plugin, file, lock, internal.createSection(path, mappings));
    }
  }

  @Override
  public String getString(@NotNull String path) {
    synchronized (lock) {
      return internal.getString(path);
    }
  }

  @Override
  public String getString(@NotNull String path, String defaultValue) {
    synchronized (lock) {
      return internal.getString(path, defaultValue);
    }
  }

  @Override
  public boolean isString(@NotNull String path) {
    synchronized (lock) {
      return internal.isString(path);
    }
  }

  @Override
  public int getInt(@NotNull String path) {
    synchronized (lock) {
      return internal.getInt(path);
    }
  }

  @Override
  public int getInt(@NotNull String path, int defaultValue) {
    synchronized (lock) {
      return internal.getInt(path, defaultValue);
    }
  }

  @Override
  public boolean isInt(@NotNull String path) {
    synchronized (lock) {
      return internal.isInt(path);
    }
  }

  @Override
  public boolean getBoolean(@NotNull String path) {
    synchronized (lock) {
      return internal.getBoolean(path);
    }
  }

  @Override
  public boolean getBoolean(@NotNull String path, boolean defaultValue) {
    synchronized (lock) {
      return internal.getBoolean(path, defaultValue);
    }
  }

  @Override
  public boolean isBoolean(@NotNull String path) {
    synchronized (lock) {
      return internal.isBoolean(path);
    }
  }

  @Override
  public double getDouble(@NotNull String path) {
    synchronized (lock) {
      return internal.getDouble(path);
    }
  }

  @Override
  public double getDouble(@NotNull String path, double defaultValue) {
    synchronized (lock) {
      return internal.getDouble(path, defaultValue);
    }
  }

  @Override
  public boolean isDouble(@NotNull String path) {
    synchronized (lock) {
      return internal.isDouble(path);
    }
  }

  @Override
  public long getLong(@NotNull String path) {
    synchronized (lock) {
      return internal.getLong(path);
    }
  }

  @Override
  public long getLong(@NotNull String path, long defaultValue) {
    synchronized (lock) {
      return internal.getLong(path, defaultValue);
    }
  }

  @Override
  public boolean isLong(@NotNull String path) {
    synchronized (lock) {
      return internal.isLong(path);
    }
  }

  @Override
  public List<?> getList(@NotNull String path) {
    synchronized (lock) {
      return internal.getList(path);
    }
  }

  @Override
  public List<?> getList(@NotNull String path, List<?> defaultValue) {
    synchronized (lock) {
      return internal.getList(path, defaultValue);
    }
  }

  @Override
  public boolean isList(@NotNull String path) {
    synchronized (lock) {
      return internal.isList(path);
    }
  }

  @Override
  public @NotNull List<String> getStringList(@NotNull String path) {
    synchronized (lock) {
      return internal.getStringList(path);
    }
  }

  @Override
  public @NotNull List<Integer> getIntegerList(@NotNull String path) {
    synchronized (lock) {
      return internal.getIntegerList(path);
    }
  }

  @Override
  public @NotNull List<Boolean> getBooleanList(@NotNull String path) {
    synchronized (lock) {
      return internal.getBooleanList(path);
    }
  }

  @Override
  public @NotNull List<Double> getDoubleList(@NotNull String path) {
    synchronized (lock) {
      return internal.getDoubleList(path);
    }
  }

  @Override
  public @NotNull List<Float> getFloatList(@NotNull String path) {
    synchronized (lock) {
      return internal.getFloatList(path);
    }
  }

  @Override
  public @NotNull List<Long> getLongList(@NotNull String path) {
    synchronized (lock) {
      return internal.getLongList(path);
    }
  }

  @Override
  public @NotNull List<Byte> getByteList(@NotNull String path) {
    synchronized (lock) {
      return internal.getByteList(path);
    }
  }

  @Override
  public @NotNull List<Character> getCharacterList(@NotNull String path) {
    synchronized (lock) {
      return internal.getCharacterList(path);
    }
  }

  @Override
  public @NotNull List<Short> getShortList(@NotNull String path) {
    synchronized (lock) {
      return internal.getShortList(path);
    }
  }

  @Override
  public @NotNull List<Map<?, ?>> getMapList(@NotNull String path) {
    synchronized (lock) {
      return internal.getMapList(path);
    }
  }

  @Override
  public <T> T getObject(@NotNull String s, @NotNull Class<T> aClass) {
    synchronized (lock) {
      return internal.getObject(s, aClass);
    }
  }

  @Override
  public <T> T getObject(@NotNull String s, @NotNull Class<T> aClass, T t) {
    synchronized (lock) {
      return internal.getObject(s, aClass, t);
    }
  }

  @Override
  public <T extends ConfigurationSerializable> T getSerializable(
      @NotNull String s, @NotNull Class<T> aClass) {
    synchronized (lock) {
      return internal.getSerializable(s, aClass);
    }
  }

  @Override
  public <T extends ConfigurationSerializable> T getSerializable(
      @NotNull String s, @NotNull Class<T> aClass, T t) {
    synchronized (lock) {
      return internal.getSerializable(s, aClass, t);
    }
  }

  @Override
  public Vector getVector(@NotNull String path) {
    synchronized (lock) {
      return internal.getVector(path);
    }
  }

  @Override
  public Vector getVector(@NotNull String path, Vector defaultValue) {
    synchronized (lock) {
      return internal.getVector(path, defaultValue);
    }
  }

  @Override
  public boolean isVector(@NotNull String path) {
    synchronized (lock) {
      return internal.isVector(path);
    }
  }

  @Override
  public OfflinePlayer getOfflinePlayer(@NotNull String path) {
    synchronized (lock) {
      return internal.getOfflinePlayer(path);
    }
  }

  @Override
  public OfflinePlayer getOfflinePlayer(@NotNull String path, OfflinePlayer defaultValue) {
    synchronized (lock) {
      return internal.getOfflinePlayer(path, defaultValue);
    }
  }

  @Override
  public boolean isOfflinePlayer(@NotNull String path) {
    synchronized (lock) {
      return internal.isOfflinePlayer(path);
    }
  }

  @Override
  public ItemStack getItemStack(@NotNull String path) {
    synchronized (lock) {
      return internal.getItemStack(path);
    }
  }

  @Override
  public ItemStack getItemStack(@NotNull String path, ItemStack defaultValue) {
    synchronized (lock) {
      return internal.getItemStack(path, defaultValue);
    }
  }

  @Override
  public boolean isItemStack(@NotNull String path) {
    synchronized (lock) {
      return internal.isItemStack(path);
    }
  }

  @Override
  public Color getColor(@NotNull String path) {
    synchronized (lock) {
      return internal.getColor(path);
    }
  }

  @Override
  public Color getColor(@NotNull String path, Color defaultValue) {
    synchronized (lock) {
      return internal.getColor(path, defaultValue);
    }
  }

  @Override
  public boolean isColor(@NotNull String path) {
    synchronized (lock) {
      return internal.isColor(path);
    }
  }

  @Override
  public @Nullable Location getLocation(@NotNull String path) {
    synchronized (lock) {
      return internal.getLocation(path);
    }
  }

  @Override
  public @Nullable Location getLocation(@NotNull String path, @Nullable Location location) {
    synchronized (lock) {
      return internal.getLocation(path, location);
    }
  }

  @Override
  public boolean isLocation(@NotNull String path) {
    synchronized (lock) {
      return internal.isLocation(path);
    }
  }

  @Override
  public ConfigurationSection getConfigurationSection(@NotNull String path) {
    synchronized (lock) {
      return new ConcurrentConfiguration(
          plugin, file, lock, internal.getConfigurationSection(path));
    }
  }

  @Override
  public boolean isConfigurationSection(@NotNull String path) {
    synchronized (lock) {
      return internal.isConfigurationSection(path);
    }
  }

  @Override
  public ConfigurationSection getDefaultSection() {
    synchronized (lock) {
      return new ConcurrentConfiguration(plugin, file, lock, internal.getDefaultSection());
    }
  }

  @Override
  public void addDefault(@NotNull String path, Object value) {
    synchronized (lock) {
      internal.addDefault(path, value);
    }
  }

  @Override
  public void addDefaults(@NotNull Map<String, Object> defaults) {
    synchronized (lock) {
      defaults.forEach(internal::addDefault);
    }
  }

  @Override
  public void addDefaults(@NotNull Configuration configuration) {
    synchronized (lock) {
      configuration.getKeys(false).forEach(key -> internal.addDefault(key, configuration.get(key)));
    }
  }

  @Override
  public Configuration getDefaults() {
    synchronized (lock) {
      return Objects.requireNonNull(internal.getRoot()).getDefaults();
    }
  }

  @Override
  public void setDefaults(@NotNull Configuration configuration) {
    synchronized (lock) {
      ConfigurationSection defaultSection = internal.getDefaultSection();
      if (defaultSection != null) {
        new HashSet<>(internal.getDefaultSection().getKeys(false))
            .forEach(key -> internal.addDefault(key, null));
      }

      configuration.getKeys(false).forEach(key -> internal.addDefault(key, configuration.get(key)));
    }
  }

  @Override
  public @NotNull ConfigurationOptions options() {
    synchronized (lock) {
      return Objects.requireNonNull(internal.getRoot()).options();
    }
  }
}
