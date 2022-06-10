package com.easterlyn;

import co.aikar.commands.BaseCommand;
import com.easterlyn.acf.EasterlynCommandManager;
import com.easterlyn.command.CoreCompletions;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.listener.UniqueListener;
import com.easterlyn.user.UserManager;
import com.easterlyn.util.BlockUpdateManager;
import com.easterlyn.util.Colors;
import com.easterlyn.util.LocaleManager;
import com.easterlyn.util.PlayerUtil;
import com.easterlyn.util.StringUtil;
import com.github.jikoo.planarwrappers.event.Event;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

/**
 * The core plugin for <a href=easterlyn.com>Easterlyn</a>'s Minecraft server.
 *
 * @author Jikoo
 */
public class EasterlynCore extends JavaPlugin {

  private final LocaleManager localeManager = new LocaleManager(this, "en_us");
  private final UserManager userManager = new UserManager(this);
  private final BlockUpdateManager blockUpdateManager = new BlockUpdateManager(this);
  private final Multimap<Class<? extends Plugin>, BaseCommand> pluginCommands =
      HashMultimap.create();
  private EasterlynCommandManager commandManager;
  private SimpleCommandMap simpleCommandMap;

  @Override
  public void onEnable() {
    saveDefaultConfig();

    Colors.load(this);

    getServer()
        .getServicesManager()
        .register(EasterlynCore.class, this, this, ServicePriority.Normal);

    if (commandManager == null) {
      commandManager = new EasterlynCommandManager(this);
      commandManager.enableUnstableAPI("help");
      commandManager.enableUnstableAPI("brigadier");
      CoreContexts.register(this);
      CoreCompletions.register(this);
      // TODO system for Group resolvers
    }

    registerCommands(this, getClassLoader(), "com.easterlyn.command");

    // Listener for preventing ruining unique items
    getServer().getPluginManager().registerEvents(new UniqueListener(localeManager), this);

    Event.register(
        PluginDisableEvent.class,
        event -> {
          if (pluginCommands.containsKey(event.getPlugin().getClass())) {
            pluginCommands
                .get(event.getPlugin().getClass())
                .forEach(commandManager::unregisterCommand);
          }
        },
        this);

    Event.register(
        ReportableEvent.class,
        event -> {
          getLogger().warning(event.getMessage());
          if (event.hasTrace()) {
            getLogger().warning(event.getTrace());
          }
        },
        this);

    Event.register(
        PlayerJoinEvent.class,
        event -> PlayerUtil.removeFromCache(event.getPlayer().getUniqueId()),
        this,
        EventPriority.LOW);
  }

  public void registerCommands(Plugin plugin, ClassLoader loader, String packageName) {
    if (!plugin.equals(this)) {
      commandManager.registerDependency(plugin.getClass(), plugin);
    }
    new Reflections(packageName, loader)
        .getSubTypesOf(BaseCommand.class).stream()
            .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
            .forEach(
                clazz -> {
                  Constructor<? extends BaseCommand> constructor;
                  BaseCommand command;
                  try {
                    constructor = clazz.getConstructor();
                    command = constructor.newInstance();
                  } catch (ReflectiveOperationException e) {
                    getLogger().severe("Unable to register command " + clazz.getName());
                    e.printStackTrace();
                    return;
                  }
                  commandManager.registerCommand(command, true);
                  if (!this.equals(plugin)) {
                    pluginCommands.put(plugin.getClass(), command);
                  }
                });
  }

  @Override
  public void onDisable() {
    userManager.clearCache();
    commandManager.unregisterCommands();
    blockUpdateManager.forceAllUpdates();
    simpleCommandMap = null;
  }

  public @NotNull EasterlynCommandManager getCommandManager() throws IllegalStateException {
    if (commandManager == null || !this.isEnabled()) {
      throw new IllegalStateException("Plugin not ready!");
    }

    return commandManager;
  }

  public LocaleManager getLocaleManager() {
    return localeManager;
  }

  public @NotNull UserManager getUserManager() throws IllegalStateException {
    return userManager;
  }

  public @NotNull BlockUpdateManager getBlockUpdateManager() {
    return blockUpdateManager;
  }

  public @Nullable SimpleCommandMap getSimpleCommandMap() {
    if (simpleCommandMap != null) {
      return simpleCommandMap;
    }

    try {
      Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
      simpleCommandMap = (SimpleCommandMap) getCommandMap.invoke(getServer());
    } catch (IllegalArgumentException
        | IllegalAccessException
        | SecurityException
        | NoSuchMethodException
        | InvocationTargetException e) {
      getLogger()
          .severe(
              "Could not fetch SimpleCommandMap from CraftServer, "
                  + "Easterlyn command will fail to register.");
      getLogger().severe(StringUtil.getTrace(e));
    }
    return simpleCommandMap;
  }
}
