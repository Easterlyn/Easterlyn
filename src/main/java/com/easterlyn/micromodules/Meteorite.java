package com.easterlyn.micromodules;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.packets.ParticleEffectWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

/**
 * Small class for creating and managing an explosive hollow sphere.
 *
 * @author Dublek, Jikoo
 */
public class Meteorite {

	/* The Plugin dropping the Meteorite. */
	private final Easterlyn plugin;
	/* The radius to drop the Meteorite within. */
	private final int radius;
	/* The Location in the sky where the Meteorite will be spawned. */
	private final Location skyTarget;
	/* Whether or not to do Block damage when the Meteorite lands. */
	private final boolean explodeBlocks;
	/* Whether or not the meteor should be in bore mode. */
	private final boolean bore;
	/* The Material to make the Meteorite be built of. */
	private Material material;
	/* The Locations of a Voxel sphere around the Meteorite's spawn Location. */
	private HashSet<Location> sphereCoords;

	/**
	 * Create a new Meteorite targeting a Location.
	 *
	 * @param target the Location to drop the Meteorite
	 * @param materialName the name of the Material to make the Meteorite out of
	 * @param radius the radius to drop the Meteorite within
	 * @param explode true if the Meteorite should explode on contact with the ground
	 */
	public Meteorite(Easterlyn plugin, Location target, String materialName, int radius, boolean explode, int bore) {
		this(plugin, target, (Material) null, radius, explode, bore);
		if (materialName != null && materialName.length() != 0) {
			this.material = Material.matchMaterial(materialName);
		}
		if (material == null) {
			this.material = Material.NETHERRACK;
		}
	}

	/**
	 * Create a new Meteorite targeting a Location.
	 *
	 * @param target the Location to drop the Meteorite
	 * @param material the Material to make the Meteorite out of
	 * @param radius the radius to drop the Meteorite within
	 * @param explode true if the Meteorite should explode on contact with the ground
	 */
	public Meteorite(Easterlyn plugin, Location target, Material material, int radius, boolean explode, int bore) {
		this.plugin = plugin;
		if (material == null) {
			material = Material.NETHERRACK;
		}
		this.material = material;
		if (radius < 0) {
			radius = 3;
		}
		this.radius = radius;

		skyTarget = target.clone();
		int highestPossible = 255 - radius;
		int highestBlock = target.getWorld().getHighestBlockYAt(target);
		int visible = highestBlock + 40 + radius;
		skyTarget.setY(visible > highestPossible ? highestPossible : visible);

		this.bore = bore == 1 || bore != 0 && target.getBlockY() < highestBlock;
		this.explodeBlocks = explode;
	}

	/**
	 * Drop the Meteorite.
	 */
	public void dropMeteorite() {
		// Meteorite cannot be dropped, tasks cannot be scheduled.
		if (!plugin.isEnabled()) {
			return;
		}

		// Decently heavy operation, should be run off the main thread where possible.
		new BukkitRunnable() {
			@Override
			public void run() {
				genMeteorite();
				if (sphereCoords.size() == 0) {
					return;
				}

				// Spawning and (where applicable) removing blocks must be done on the main thread
				new BukkitRunnable() {
					@Override
					public void run() {
						ParticleUtils particles = plugin.getModule(ParticleUtils.class);
						for (Location location : sphereCoords) {
							particles.addEntity(new MeteoriteComponent(location, material, explodeBlocks, bore).getBukkitEntity(),
									new ParticleEffectWrapper(Particle.LAVA, 1));
						}
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Generates the Location sphere that represents a Meteorite.
	 */
	private void genMeteorite() {
		if (sphereCoords != null) {
			return;
		}
		sphereCoords = this.genSphereCoords(radius);
		sphereCoords.removeAll(this.genSphereCoords(radius - 1));
	}

	/**
	 * Generates a Set of the Locations in a hollow Voxel sphere around a Location.
	 *
	 * @param radius the radius of the sphere
	 * @return the HashSet generated
	 */
	private HashSet<Location> genSphereCoords(int radius) {
		HashSet<Location> coords = new HashSet<>();
		if (radius < 0) {
			return coords;
		}
		double radiusSquared = Math.pow(radius, 2);
		double x = skyTarget.getX();
		double y = skyTarget.getY();
		double z = skyTarget.getZ();

		for (int dZ = 0; dZ <= radius; dZ++) {
			double dZSquared = Math.pow(dZ, 2);
			for (int dX = 0; dX <= radius; dX++) {
				double dXSquared = Math.pow(dX, 2);
				for (int dY = 0; dY <= radius; dY++) {
					if ((dXSquared + Math.pow(dY, 2) + dZSquared) <= radiusSquared) {
						coords.add(new Location(skyTarget.getWorld(), x + dX, y + dY, z + dZ));
						coords.add(new Location(skyTarget.getWorld(), x + dX, y + dY, z - dZ));
						coords.add(new Location(skyTarget.getWorld(), x - dX, y + dY, z + dZ));
						coords.add(new Location(skyTarget.getWorld(), x - dX, y + dY, z - dZ));
						coords.add(new Location(skyTarget.getWorld(), x + dX, y - dY, z + dZ));
						coords.add(new Location(skyTarget.getWorld(), x + dX, y - dY, z - dZ));
						coords.add(new Location(skyTarget.getWorld(), x - dX, y - dY, z + dZ));
						coords.add(new Location(skyTarget.getWorld(), x - dX, y - dY, z - dZ));
					}
				}
			}
		}
		return coords;
	}

}
