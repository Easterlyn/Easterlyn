package com.easterlyn.util.wrapper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
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
import org.jetbrains.annotations.Contract;
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
  private final AtomicBoolean dirty = new AtomicBoolean(false);
  private final AtomicReference<BukkitTask> saveTask = new AtomicReference<>();

  private ConcurrentConfiguration(
      Plugin plugin, @Nullable File file, Object lock, ConfigurationSection section) {
    this.plugin = plugin;
    this.file = file;
    this.lock = lock;
    this.internal = section;
  }

  @Contract("_, _ -> new")
  public static @NotNull ConcurrentConfiguration load(@NotNull Plugin plugin, @Nullable File file) {
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
    if (file == null || saveTask.get() != null || !dirty.get()) {
      return;
    }

    try {
      saveTask.set(
          new BukkitRunnable() {
            @Override
            public void run() {
              try {
                saveNow(file);
              } catch (IOException e) {
                e.printStackTrace();
              }
              saveTask.set(null);
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
              super.cancel();
              try {
                saveNow(file);
              } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error saving user data", e);
              }
              saveTask.set(null);
            }
          }.runTaskLaterAsynchronously(plugin, 200L));
    } catch (IllegalStateException e) {
      // Plugin is being disabled, cannot schedule tasks
      saveTask.set(null);
      try {
        saveNow(file);
      } catch (IOException ioe) {
        plugin.getLogger().log(Level.WARNING, "Error saving user data", ioe);
      }
    }
  }

  private void saveNow(@NotNull File file) throws IOException {
    synchronized (lock) {
      Configuration root = internal.getRoot();

      if (root == null) {
        // Section has been orphaned. Parent will handle the save.
        return;
      }

      Path filePath = file.toPath();
      Path parent = filePath.getParent();
      Files.createDirectories(parent);

      String data;
      if (root instanceof FileConfiguration fileConfiguration) {
        data = fileConfiguration.saveToString();
      } else {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.setDefaults(root);
        configuration.options().copyDefaults(true);
        data = configuration.saveToString();
      }

      Path tempPath = filePath.resolveSibling(filePath.getFileName() + ".tmp");
      try (Writer writer = new OutputStreamWriter(
          Files.newOutputStream(tempPath),
          StandardCharsets.UTF_8)) {
        writer.write(data);
        Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      }
      this.dirty.set(false);
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
  public @Nullable Configuration getRoot() {
    synchronized (lock) {
      Configuration root = internal.getRoot();
      if (root == null) {
        return null;
      }
      return new ConcurrentConfiguration(plugin, file, lock, root);
    }
  }

  @Override
  public @Nullable ConfigurationSection getParent() {
    synchronized (lock) {
      ConfigurationSection parent = internal.getParent();
      if (parent == null) {
        return null;
      }
      return new ConcurrentConfiguration(plugin, file, lock, parent);
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
      dirty.set(true);
      save();
    }
  }

  @Override
  public @NotNull ConfigurationSection createSection(@NotNull String path) {
    synchronized (lock) {
      ConfigurationSection internalSection = internal.createSection(path);
      dirty.set(true);
      save();
      return new ConcurrentConfiguration(plugin, file, lock, internalSection);
    }
  }

  @Override
  public @NotNull ConfigurationSection createSection(
      @NotNull String path, @NotNull Map<?, ?> mappings) {
    synchronized (lock) {
      ConfigurationSection internalSection = internal.createSection(path, mappings);
      dirty.set(true);
      save();
      return new ConcurrentConfiguration(plugin, file, lock, internalSection);
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
  public <T> T getObject(@NotNull String s, @NotNull Class<T> clazz) {
    synchronized (lock) {
      return internal.getObject(s, clazz);
    }
  }

  @Override
  public <T> T getObject(@NotNull String s, @NotNull Class<T> clazz, T t) {
    synchronized (lock) {
      return internal.getObject(s, clazz, t);
    }
  }

  @Override
  public <T extends ConfigurationSerializable> T getSerializable(
      @NotNull String s, @NotNull Class<T> clazz) {
    synchronized (lock) {
      return internal.getSerializable(s, clazz);
    }
  }

  @Override
  public <T extends ConfigurationSerializable> T getSerializable(
      @NotNull String s, @NotNull Class<T> clazz, T t) {
    synchronized (lock) {
      return internal.getSerializable(s, clazz, t);
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
  public @Nullable ConfigurationSection getConfigurationSection(@NotNull String path) {
    synchronized (lock) {
      ConfigurationSection configurationSection = internal.getConfigurationSection(path);
      if (configurationSection == null) {
        return null;
      }
      return new ConcurrentConfiguration(plugin, file, lock, configurationSection);
    }
  }

  @Override
  public boolean isConfigurationSection(@NotNull String path) {
    synchronized (lock) {
      return internal.isConfigurationSection(path);
    }
  }

  @Override
  public @Nullable ConfigurationSection getDefaultSection() {
    synchronized (lock) {
      ConfigurationSection defaultSection = internal.getDefaultSection();
      if (defaultSection == null) {
        return null;
      }
      return new ConcurrentConfiguration(plugin, file, lock, defaultSection);
    }
  }

  @Override
  public void addDefault(@NotNull String path, Object value) {
    synchronized (lock) {
      internal.addDefault(path, value);
    }
  }

  @NotNull
  @Override
  public List<String> getComments(@NotNull String path) {
    synchronized (lock) {
      return internal.getComments(path);
    }
  }

  @NotNull
  @Override
  public List<String> getInlineComments(@NotNull String path) {
    synchronized (lock) {
      return internal.getInlineComments(path);
    }
  }

  @Override
  public void setComments(@NotNull String path, @Nullable List<String> comments) {
    synchronized (lock) {
      internal.setComments(path, comments);
      dirty.set(true);
      save();
    }
  }

  @Override
  public void setInlineComments(@NotNull String path, @Nullable List<String> comments) {
    synchronized (lock) {
      internal.setInlineComments(path, comments);
      dirty.set(true);
      save();
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
