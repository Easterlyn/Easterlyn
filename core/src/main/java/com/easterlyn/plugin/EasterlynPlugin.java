package com.easterlyn.plugin;

import com.easterlyn.EasterlynCore;
import com.github.jikoo.planarwrappers.event.Event;
import java.util.Objects;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * A JavaPlugin extension for plugins dependent on the Easterlyn core plugin.
 */
public abstract class EasterlynPlugin extends JavaPlugin {

  @Override
  public final void onEnable() {
    enable();
    register(getCore());

    Event.register(
        PluginEnableEvent.class,
        event -> {
          if (event.getPlugin() instanceof EasterlynCore) {
            register((EasterlynCore) event.getPlugin());
          }
        },
        this);
  }

  /**
   * Perform enable operations.
   */
  protected abstract void enable();

  /**
   * Register relevant details with core plugin.
   *
   * @param core the core plugin instance
   */
  protected abstract void register(@NotNull EasterlynCore core);

  /**
   * Get the active EasterlynCore instance.
   *
   * @return the instance
   */
  public @NotNull EasterlynCore getCore() {
    RegisteredServiceProvider<EasterlynCore> registration =
        getServer().getServicesManager().getRegistration(EasterlynCore.class);
    return Objects.requireNonNull(registration).getProvider();
  }

}
