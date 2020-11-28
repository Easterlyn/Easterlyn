package com.easterlyn.util;

import com.easterlyn.util.protection.ProtectionHook;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.reflections.Reflections;

public class ProtectionUtil {

  private static final Set<ProtectionHook> HOOKS = new HashSet<>();

  static {
    new Reflections("com.easterlyn.util.protection")
        .getSubTypesOf(ProtectionHook.class).stream()
            .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
            .forEach(
                clazz -> {
                  try {
                    HOOKS.add(clazz.getConstructor().newInstance());
                  } catch (NoClassDefFoundError ignored) {
                    // Dependency not present
                  } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                  }
                });
  }

  public static boolean isProtected(Location location) {
    return HOOKS.stream().anyMatch(hook -> hook.isHookUsable() && hook.isProtected(location));
  }

  public static boolean canMobsSpawn(Location location) {
    return HOOKS.stream().anyMatch(hook -> hook.isHookUsable() && hook.canMobsSpawn(location));
  }

  public static boolean canUseButtonsAt(Player player, Location location) {
    return HOOKS.stream()
        .anyMatch(hook -> hook.isHookUsable() && hook.canUseButtonsAt(player, location));
  }

  public static boolean canOpenChestsAt(Player player, Location location) {
    return HOOKS.stream()
        .anyMatch(hook -> hook.isHookUsable() && hook.canOpenChestsAt(player, location));
  }

  public static boolean canBuildAt(Player player, Location location) {
    return HOOKS.stream()
        .anyMatch(hook -> hook.isHookUsable() && hook.canBuildAt(player, location));
  }
}
