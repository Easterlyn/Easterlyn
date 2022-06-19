package com.easterlyn.util;

import com.easterlyn.util.text.QuoteConsumer;
import com.easterlyn.util.text.TextParsing;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocaleManager {

  private final Plugin plugin;
  private final Set<Plugin> resourceLoaders;
  private final String defaultLocale;
  private final Map<String, YamlConfiguration> locales;

  public LocaleManager(@NotNull Plugin plugin, @NotNull String defaultLocale) {
    this.plugin = plugin;
    this.defaultLocale = defaultLocale;
    this.locales = new HashMap<>();
    resourceLoaders = new HashSet<>();
    resourceLoaders.add(plugin);
  }

  public void addLocaleSupplier(@NotNull Plugin plugin) {
    this.resourceLoaders.add(plugin);
    this.locales.clear();
  }

  private @NotNull YamlConfiguration getOrLoadLocale(@NotNull String locale) {
    YamlConfiguration loaded = locales.get(locale);
    if (loaded != null) {
      return loaded;
    }

    YamlConfiguration aggregated = new YamlConfiguration();
    for (Plugin plugin : resourceLoaders) {
      InputStream resourceStream = plugin.getResource(locale + ".yml");
      if (resourceStream == null) {
        continue;
      }

      YamlConfiguration localeConfigDefaults;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
        localeConfigDefaults = YamlConfiguration.loadConfiguration(reader);
      } catch (IOException e) {
        plugin
            .getLogger()
            .log(Level.WARNING, "[LocaleManager] Unable to load resource " + locale + ".yml", e);
        localeConfigDefaults = new YamlConfiguration();
      }

      File file = new File(plugin.getDataFolder(), locale + ".yml");
      YamlConfiguration localeConfig;

      if (!file.exists()) {
        localeConfig = localeConfigDefaults;
        try {
          localeConfigDefaults.save(file);
        } catch (IOException e) {
          plugin
              .getLogger()
              .log(Level.WARNING, "[LocaleManager] Unable to save resource " + locale + ".yml", e);
        }
      } else {
        localeConfig = YamlConfiguration.loadConfiguration(file);

        // Add new language keys
        List<String> newKeys = new ArrayList<>();
        for (String key : localeConfigDefaults.getKeys(true)) {
          if (localeConfigDefaults.isConfigurationSection(key)) {
            continue;
          }

          if (localeConfig.isSet(key)) {
            continue;
          }

          localeConfig.set(key, localeConfigDefaults.get(key));
          newKeys.add(key);
        }

        if (!newKeys.isEmpty()) {
          plugin
              .getLogger()
              .info("[LocaleManager] Added new language keys: " + String.join(", ", newKeys));
          try {
            aggregated.save(new File(plugin.getDataFolder(), locale + ".yml"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      for (String key : localeConfig.getKeys(true)) {
        if (!localeConfig.isConfigurationSection(key)) {
          aggregated.set(key, localeConfig.get(key));
        }
      }
    }

    // Colorize messages after saving - don't write color codes to config
    for (String key : aggregated.getKeys(true)) {
      if (!aggregated.isString(key)) {
        continue;
      }
      String value = aggregated.getString(key);
      if (value != null) {
        aggregated.set(key, Colors.addColor(value));
      }
    }

    if (!locale.equals(defaultLocale)) {
      YamlConfiguration localeConfigDefaults = getOrLoadLocale(defaultLocale);

      // Check for missing keys
      List<String> newKeys = new ArrayList<>();
      for (String key : localeConfigDefaults.getKeys(true)) {
        if (localeConfigDefaults.isConfigurationSection(key)) {
          continue;
        }

        if (aggregated.isSet(key)) {
          continue;
        }

        newKeys.add(key);
      }

      if (!newKeys.isEmpty()) {
        plugin
            .getLogger()
            .info(
                "[LocaleManager] Missing translations from "
                    + locale
                    + ": "
                    + String.join(", ", newKeys));
      }

      // Fall through to default locale
      aggregated.setDefaults(localeConfigDefaults);
    }

    locales.put(locale, aggregated);
    return aggregated;
  }

  public void sendMessage(
      @Nullable CommandSender sender, @NotNull String key, @NotNull String... replacements) {
    sendMessage(sender, ChatMessageType.CHAT, key, Collections.emptyList(), replacements);
  }

  public void sendMessage(
      @Nullable CommandSender sender,
      @NotNull String key,
      @NotNull Collection<QuoteConsumer> additionalHandlers) {
    sendMessage(sender, ChatMessageType.CHAT, key, additionalHandlers);
  }

  public void sendMessage(
      @Nullable CommandSender sender,
      @NotNull ChatMessageType type,
      @NotNull String key,
      @NotNull String... replacements) {
    sendMessage(sender, type, key, Collections.emptyList(), replacements);
  }

  public void sendMessage(
      @Nullable CommandSender sender,
      @NotNull ChatMessageType type,
      @NotNull String key,
      @NotNull Collection<QuoteConsumer> additionalHandlers,
      @NotNull String... replacements) {
    if (sender == null) {
      return;
    }
    String message = getValue(key, getLocale(sender), replacements);
    if (message == null || message.isEmpty()) {
      return;
    }

    Collection<TextComponent> textComponents = TextParsing.toJSON(message, additionalHandlers);
    if (sender instanceof Player player) {
      if (type == ChatMessageType.ACTION_BAR) {
        player
            .spigot()
            .sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(textComponents.toArray(new BaseComponent[0])));
      } else {
        sender
            .spigot()
            .sendMessage(new TextComponent(textComponents.toArray(new BaseComponent[0])));
      }
      return;
    }

    StringBuilder simpleMessage = new StringBuilder();
    for (TextComponent component : textComponents) {
      simpleMessageHelper(simpleMessage, component);
    }

    sender.sendMessage(simpleMessage.toString());
  }

  private void simpleMessageHelper(StringBuilder builder, TextComponent component) {
    ClickEvent clickEvent = component.getClickEvent();
    if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
      builder.append(clickEvent.getValue());
    } else {
      builder.append(component.getText());
    }
    List<BaseComponent> extra = component.getExtra();
    if (extra != null) {
      for (BaseComponent extraComponent : extra) {
        if (extraComponent instanceof TextComponent) {
          simpleMessageHelper(builder, (TextComponent) extraComponent);
        }
      }
    }
  }

  public void broadcast(@NotNull String key, @NotNull String... replacements) {
    broadcast(ChatMessageType.CHAT, key, Collections.emptyList(), replacements);
  }

  public void broadcast(
      @NotNull String key, @NotNull Collection<QuoteConsumer> additionalHandlers) {
    broadcast(ChatMessageType.CHAT, key, additionalHandlers);
  }

  public void broadcast(
      @NotNull ChatMessageType type, @NotNull String key, @NotNull String... replacements) {
    broadcast(type, key, Collections.emptyList(), replacements);
  }

  public void broadcast(
      @NotNull ChatMessageType type,
      @NotNull String key,
      @NotNull Collection<QuoteConsumer> additionalHandlers,
      @NotNull String... replacements) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      sendMessage(player, type, key, additionalHandlers, replacements);
    }
    sendMessage(Bukkit.getConsoleSender(), type, key, additionalHandlers, replacements);
  }

  public @Nullable String getValue(@NotNull String key) {
    return getValue(key, defaultLocale);
  }

  public @Nullable String getValue(@NotNull String key, @NotNull String locale) {
    String value = getOrLoadLocale(locale).getString(key);
    if (value == null || value.isEmpty()) {
      return null;
    }

    value = Colors.addColor(value);

    return value;
  }

  public @Nullable String getValue(
      @NotNull String key, @NotNull String locale, String @NotNull ... replacements) {
    if (replacements.length % 2 != 0) {
      plugin
          .getLogger()
          .log(Level.WARNING, "[LocaleManager] Replacement data is uneven", new Exception());
    }

    String value = getValue(key, locale);

    if (value == null) {
      return null;
    }

    for (int i = 0; i < replacements.length; i += 2) {
      String replacement = replacements[i + 1];
      if (replacement == null) {
        replacement = "null";
      }
      value = value.replace(replacements[i], replacement);
    }

    return value;
  }

  public String getLocale(@Nullable CommandSender sender) {
    return sender instanceof Player ? ((Player) sender).getLocale().toLowerCase(Locale.ENGLISH) : defaultLocale;
  }

  public String getDefaultLocale() {
    return defaultLocale;
  }

}
