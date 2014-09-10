package co.sblock.utilities.meteors;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import co.sblock.Sblock;
import co.sblock.events.packets.ParticleUtils;
import co.sblock.events.packets.WrapperPlayServerWorldParticles.ParticleEffect;

/**
 * @author Dublek, Jikoo
 */
public class Meteorite {

	/** The radius to drop the Meteorite within. */
	private int radius;
	/** The Location in the sky where the Meteorite will be spawned. */
	private Location skyTarget;
	/** Whether or not to do Block damage when the Meteorite lands. */
	private boolean explosionBlockDamage;
	/** Whether or not the meteor should be in bore mode. */
	private boolean boreMode;
	/** The Material to make the Meteorite be built of. */
	private Material mat;
	/** The Locations of a Voxel sphere around the Meteorite's spawn Location. */
	private HashSet<Location> sphereCoords;
	/** The Task ID for dropping a Meteorite. */
	private int dropTask;

	/**
	 * Create a new Meteorite targeting a Player or Location.
	 * 
	 * @param target the Location to drop the Meteorite
	 * @param m the name of the Material to make the Meteorite out of
	 * @param r the radius to drop the Meteorite within
	 * @param explode true if the Meteorite should explode on contact with the ground
	 */
	public Meteorite(Location target, String m, int r, boolean explode, int bore) {

		skyTarget = target.clone();
		int highestPossible = 255 - radius;
		int highestBlock = target.getWorld().getHighestBlockYAt(target);
		int visible = highestBlock + 40 + r;
		skyTarget.setY(visible > highestPossible ? highestPossible : visible);

		if (r > 0) {
			radius = r;
		} else {
			radius = 3;
		}
		if (m != null && m.length() != 0) {
			mat = Material.matchMaterial(m);
		}
		if (mat == null) {
			mat = Material.NETHERRACK;
		}
		if (bore == -1) {
			boreMode = target.getBlockY() < highestBlock;
		} else {
			boreMode = bore == 2 ? true : false;
		}
		explosionBlockDamage = explode;
		dropTask = -1;
	}

	/**
	 * Drop the Meteorite, or, if there is still time remaining on
	 * the countdown, tick the timer.
	 */
	@SuppressWarnings("deprecation")
	public void dropMeteorite() {
		// Meteorite cannot be dropped, tasks cannot be scheduled.
		if (!Sblock.getInstance().isEnabled()) {
			this.removeMeteor();
			return;
		}

		// Decently heavy operation, should be run off the main thread where possible.
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				genMeteorite();
				if (sphereCoords.size() == 0) {
					return;
				}

				// Spawning and (where applicable) removing blocks must be done on the main thread
				Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
					@Override
					public void run() {
						ParticleUtils pu = ParticleUtils.getInstance();
						for (Location location : sphereCoords) {
							if (location.getBlock().getType() == mat) {
								location.getBlock().setType(Material.AIR);
							}
							pu.addEntity(new MeteoriteComponent(location, mat, explosionBlockDamage,
									boreMode).getBukkitEntity(), ParticleEffect.LAVA);
						}
						MeteorMod.getInstance().getLogger().info(
								"Meteor: " + skyTarget.getBlockX() + ", " + skyTarget.getBlockZ());
					}
				});
			}
		});
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
	@SuppressWarnings("deprecation")
	public void hoverMeteorite(final long hoverTicks) {
		// Generation is a heavy operation - do it off the main thread.
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				genMeteorite();
				if (sphereCoords.size() == 0) {
					return;
				}

				// Construct the meteor in the world - must be on the main thread
				Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
					@Override
					public void run() {
						for (Location a : sphereCoords) {
							if (a.getBlock().isEmpty()) {
								a.getBlock().setType(mat);
							}
						}
					}
				});

				// Schedule the meteorite to drop later
				dropTask = Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
					@Override
					public void run() {
						dropMeteorite();
						dropTask = -1;
					}
				}, hoverTicks);
			}
		});
	}

	/**
	 * Generates a Set of the Locations in a hollow Voxel sphere around a Location.
	 * 
	 * @param r the radius of the sphere
	 * @return the HashSet generated
	 */
	private HashSet<Location> genSphereCoords(int r) {
		HashSet<Location> coords = new HashSet<>();
		double bpow = Math.pow(r, 2);
		double bx = skyTarget.getX();
		double by = skyTarget.getY();
		double bz = skyTarget.getZ();

		for (int z = 0; z <= r; z++) {
			double zpow = Math.pow(z, 2);
			for (int x = 0; x <= r; x++) {
				double xpow = Math.pow(x, 2);
				for (int y = 0; y <= r; y++) {
					if ((xpow + Math.pow(y, 2) + zpow) <= bpow) {
						coords.add(new Location(skyTarget.getWorld(), bx + x, by + y, bz + z));
						coords.add(new Location(skyTarget.getWorld(), bx + x, by + y, bz - z));
						coords.add(new Location(skyTarget.getWorld(), bx - x, by + y, bz + z));
						coords.add(new Location(skyTarget.getWorld(), bx - x, by + y, bz - z));
						coords.add(new Location(skyTarget.getWorld(), bx + x, by - y, bz + z));
						coords.add(new Location(skyTarget.getWorld(), bx + x, by - y, bz - z));
						coords.add(new Location(skyTarget.getWorld(), bx - x, by - y, bz + z));
						coords.add(new Location(skyTarget.getWorld(), bx - x, by - y, bz - z));
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
		return dropTask == -1;
	}

	/**
	 * Removes the hovered meteorite, if any.
	 */
	public void removeMeteor() {
		if (hasDropped()) {
			return;
		}
		Bukkit.getScheduler().cancelTask(dropTask);
		for (Location location : sphereCoords) {
			if (location.getBlock().getType() == mat) {
				location.getBlock().setType(Material.AIR);
			}
		}
	}

	/**
	 * Gets the Meteorite dropping task.
	 * 
	 * @return the dropTask ID
	 */
	public int getDropTask() {
		return dropTask;
	}
}
