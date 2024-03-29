package com.easterlyn.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class HologramUtil {

  // It's this or static abuse.
  private static final NamespacedKey KEY = new NamespacedKey("easterlyn", "hologram");

  private HologramUtil() {}

  public static @Nullable ArmorStand getOrCreateHologram(Location location) {
    return getHologram(location, true);
  }

  public static @Nullable ArmorStand getHologram(Location location) {
    return getHologram(location, false);
  }

  private static @Nullable ArmorStand getHologram(Location location, boolean create) {
    if (location.getWorld() == null) {
      return null;
    }
    for (Entity entity : location.getWorld().getNearbyEntities(location, 0.1, 0.1, 0.1)) {
      if (isHologram(entity)) {
        return (ArmorStand) entity;
      }
    }
    return create ? makeHologram(location) : null;
  }

  private static @Nullable ArmorStand makeHologram(Location location) {
    if (location.getWorld() == null) {
      return null;
    }
    ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
    stand.setGravity(false);
    stand.setMarker(true);
    stand.setVisible(false);
    stand.setCustomNameVisible(true);
    stand.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 0);
    return stand;
  }

  private static boolean isHologram(Entity entity) {
    return entity instanceof ArmorStand
        && entity.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE);
  }
}
