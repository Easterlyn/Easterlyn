package com.easterlyn.util.protection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Abstraction for a hook to protection plugins.
 *
 * @author Jikoo
 */
public abstract class ProtectionHook {

  private final String pluginName;

  ProtectionHook(String pluginName) {
    this.pluginName = pluginName;
  }

  public boolean isHookUsable() {
    return Bukkit.getPluginManager().isPluginEnabled(pluginName);
  }

  public abstract boolean isProtected(Location location);

  public abstract boolean canMobsSpawn(Location location);

  public abstract boolean canUseButtonsAt(Player player, Location location);

  public abstract boolean canOpenChestsAt(Player player, Location location);

  public abstract boolean canBuildAt(Player player, Location location);
}
