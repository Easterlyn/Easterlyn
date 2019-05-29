package com.easterlyn.micromodules;

import com.easterlyn.Easterlyn;
import com.easterlyn.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

/**
 * Module for Holograms while Machines undergo a rework.
 *
 * @author Jikoo
 */
public class Holograms extends Module {

	private final NamespacedKey key;

	public Holograms(Easterlyn plugin) {
		super(plugin);
		key = new NamespacedKey(getPlugin(), "hologram");
	}

	@Override
	protected void onEnable() { }

	@Override
	protected void onDisable() {
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (isHologram(entity)) {
					entity.remove();
				}
			}
		}
	}

	public @Nullable ArmorStand getHologram(Location location) {
		return getHologram(location, false);
	}

	public @Nullable
	ArmorStand getOrCreateHologram(Location location) {
		return getHologram(location, true);
	}

	private ArmorStand getHologram(Location location, boolean create) {
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

	private ArmorStand makeHologram(Location location) {
		if (location.getWorld() == null) {
			return null;
		}
		ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
		stand.setGravity(false);
		stand.setMarker(true);
		stand.setVisible(false);
		stand.setCustomNameVisible(true);
		stand.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 0);
		return stand;
	}

	public void removeHolograms(Chunk chunk) {
		for (Entity entity : chunk.getEntities()) {
			if (isHologram(entity)) {
				entity.remove();
			}
		}
	}

	private boolean isHologram(Entity entity) {
		return entity instanceof ArmorStand && entity.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public String getName() {
		return "Holograms";
	}

}
