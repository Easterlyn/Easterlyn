package com.easterlyn.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class HologramUtil {

	@SuppressWarnings("deprecation") // It's this or static abuse.
	private static final NamespacedKey KEY = new NamespacedKey("easterlyn", "hologram");

	@Nullable
	public static ArmorStand getHologram(Location location) {
		return getHologram(location, false);
	}

	@Nullable
	public static ArmorStand getOrCreateHologram(Location location) {
		return getHologram(location, true);
	}

	private static ArmorStand getHologram(Location location, boolean create) {
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

	private static ArmorStand makeHologram(Location location) {
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
		return entity instanceof ArmorStand && entity.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE);
	}

}
