package co.sblock.micromodules;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

import co.sblock.Sblock;
import co.sblock.module.Module;

/**
 * Module for Holograms while Machines undergo a rework.
 * 
 * @author Jikoo
 */
public class Holograms extends Module {

	public Holograms(Sblock plugin) {
		super(plugin);
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

	public ArmorStand makeHologram(Location location) {
		ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
		stand.setGravity(false);
		stand.setMarker(true);
		stand.setVisible(false);
		stand.setCustomNameVisible(true);
		stand.setMetadata("SblockHolo", new FixedMetadataValue(getPlugin(), true));
		return stand;
	}

	public void removeHolograms(Chunk chunk) {
		for (Entity entity : chunk.getEntities()) {
			if (isHologram(entity)) {
				entity.remove();
			}
		}
	}

	public ArmorStand getHologram(Location location) {
		return getHologram(location, false);
	}

	public ArmorStand getOrCreateHologram(Location location) {
		return getHologram(location, true);
	}

	private ArmorStand getHologram(Location location, boolean create) {
		for (Entity entity : location.getWorld().getNearbyEntities(location, 0.1, 0.1, 0.1)) {
			if (isHologram(entity)) {
				return (ArmorStand) entity;
			}
		}
		return create ? makeHologram(location) : null;
	}

	public boolean isHologram(Entity entity) {
		return entity instanceof ArmorStand && entity.hasMetadata("SblockHolo");
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
