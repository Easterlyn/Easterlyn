package co.sblock.micromodules;

import java.util.HashSet;

import co.sblock.Sblock;
import co.sblock.events.packets.ParticleEffectWrapper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Small class for creating and managing an explosive hollow sphere.
 * 
 * @author Dublek, Jikoo
 */
public class Meteorite {

	/* The Plugin dropping the Meteorite. */
	private final Sblock plugin;
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
	/* The BukkitTask for dropping the Meteorite on a delay. */
	private BukkitTask dropTask;

	/**
	 * Create a new Meteorite targeting a Location.
	 * 
	 * @param target the Location to drop the Meteorite
	 * @param materialName the name of the Material to make the Meteorite out of
	 * @param radius the radius to drop the Meteorite within
	 * @param explode true if the Meteorite should explode on contact with the ground
	 */
	public Meteorite(Sblock plugin, Location target, String materialName, int radius, boolean explode, int bore) {
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
	public Meteorite(Sblock plugin, Location target, Material material, int radius, boolean explode, int bore) {
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

		this.bore = bore == 1 ? true : bore == 0 ? false : target.getBlockY() < highestBlock;
		this.explodeBlocks = explode;
	}

	/**
	 * Drop the Meteorite.
	 */
	public void dropMeteorite() {
		// Meteorite cannot be dropped, tasks cannot be scheduled.
		if (!plugin.isEnabled()) {
			this.removeMeteorite();
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
							if (location.getBlock().getType() == material) {
								location.getBlock().setType(Material.AIR);
							}
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
	public void genMeteorite() {
		if (sphereCoords == null) {
			sphereCoords = this.genSphereCoords(radius);
		}
		sphereCoords.removeAll(this.genSphereCoords(radius - 1));
	}

	/**
	 * Sets all blocks in the Meteorite to the Meteorite's Material.
	 */
	public void hoverMeteorite(final long hoverTicks) {
		// Generation is a heavy operation - do it off the main thread.
		new BukkitRunnable() {
			@Override
			public void run() {
				genMeteorite();
				if (sphereCoords.size() == 0) {
					return;
				}

				// Construct the meteor in the world - must be on the main thread
				new BukkitRunnable() {
					@Override
					public void run() {
						for (Location a : sphereCoords) {
							if (a.getBlock().isEmpty()) {
								a.getBlock().setType(material);
							}
						}
					}
				}.runTask(plugin);

				// Schedule the meteorite to drop later
				dropTask = new BukkitRunnable() {
					@Override
					public void run() {
						dropMeteorite();
					}
				}.runTaskLater(plugin, hoverTicks);
			}
		}.runTaskAsynchronously(plugin);
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

	/**
	 * Check if the Meteorite has been dropped. Applicable only when hoverMeteorite has been called.
	 * 
	 * @return true if a task is pending to drop the Meteorite.
	 */
	public boolean hasDropped() {
		return dropTask != null && dropTask.getTaskId() > 0;
	}

	/**
	 * Removes the hovered meteorite, if any.
	 */
	public void removeMeteorite() {
		if (hasDropped()) {
			return;
		}
		dropTask.cancel();
		for (Location location : sphereCoords) {
			if (location.getBlock().getType() == material) {
				location.getBlock().setType(Material.AIR);
			}
		}
	}
}
