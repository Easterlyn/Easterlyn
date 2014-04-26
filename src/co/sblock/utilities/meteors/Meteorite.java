package co.sblock.utilities.meteors;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Listener;
import co.sblock.Sblock;

/**
 * @author Dublek, Jikoo
 */
public class Meteorite implements Listener {

	/** The radius to drop the Meteorite within. */
	private int radius;
	/** The Location in the sky where the Meteorite will be spawned. */
	private Location skyTarget;
	/** Whether or not to do Block damage when the Meteorite lands. */
	private boolean explosionBlockDamage;
	/** The Material to make the Meteorite be built of. */
	private Material mat;
	/** The Locations of a Voxel sphere around the Meteorite's spawn Location. */
	private HashSet<Location> sphereCoords;

	/**
	 * Create a new Meteorite targeting a Player or Location.
	 * 
	 * @param target the Location to drop the Meteorite
	 * @param m the name of the Material to make the Meteorite out of
	 * @param r the radius to drop the Meteorite within
	 * @param explode true if the Meteorite should explode on contact with the ground
	 */
	public Meteorite(Location target, String m, int r, boolean explode) {

		skyTarget = target.clone();
		int highestPossible = 255 - radius;
		int visible = target.getWorld().getHighestBlockYAt(target) + 40;
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
		explosionBlockDamage = explode;
		// module = instance;
		Bukkit.getPluginManager().registerEvents(this, Sblock.getInstance());
	}

	/**
	 * Drop the Meteorite, or, if there is still time remaining on
	 * the countdown, tick the timer.
	 */
	@SuppressWarnings("deprecation")
	public void dropMeteorite() {
		// Decently heavy operation, should be run off the main thread where possible.
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				genMeteorite();
				if (sphereCoords.size() < 1) {
					return;
				}

				// The rest must be done on the main thread. Yes, this is hideous. Get over it.
				Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
					@Override
					public void run() {
						MeteorMod mm = MeteorMod.getInstance();
						for (Location location : sphereCoords) {
							if (location.getBlock().getType() == mat) {
								location.getBlock().setType(Material.AIR);
							}
							FallingBlock f = skyTarget.getWorld().spawnFallingBlock(location, mat, (byte) 0);
							f.setDropItem(false);
							mm.addEntity(f, explosionBlockDamage);
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
	public void hoverMeteorite() {
		for (Location a : sphereCoords) {
			if (a.getBlock().isEmpty()) {
				a.getBlock().setType(mat);
			}
		}
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
}
