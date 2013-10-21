package co.sblock.Sblock.Utilities.MeteorMod;

import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import co.sblock.Sblock.Sblock;

/**
 * @author Dublek
 */
public class Meteorite implements Listener {

	// private MeteorMod module;
	/** The current countdown number. */
	private int countdown;
	/** The radius to drop the <code>Meteorite</code> within. */
	private int radius;
	/** The default radius. */
	private final int DEFAULT_RADIUS = 3;
	/** The default countdown length. */
	private final int DEFAULT_COUNTDOWN = 0;
	/** The <code>Meteorite</code>'s target <code>Location</code>. */
	private Location target;
	/**
	 * The <code>Location</code> in the sky where the <code>Meteorite</code>
	 * will be spawned.
	 */
	private Location skyTarget;
	/** The <code>Meteorite</code>'s target <code>Player</code>. */
	private Player pTarget;
	/**
	 * Whether or not to do <code>Block</code> damage when the
	 * <code>Meteorite</code> lands.
	 */
	private boolean explosionBlockDamage;
	/**
	 * The <code>Material</code> to make the <code>Meteorite</code> be built of.
	 */
	private Material mat;
	/** The default <code>Meteorite</code> <code>Material</code>. */
	private final Material DEFAULT_MAT = Material.NETHERRACK;
	/**
	 * The <code>Location</code>s of a <code>Voxel</code> sphere around the
	 * <code>Meteorite</code>'s spawn Location.
	 */
	private ArrayList<Location> sphereCoords = new ArrayList<Location>();
	/**
	 * An <code>ArrayList</code> of the <code>UUID</code> of every
	 * <code>Entity</code> in the <code>Meteorite</code>.
	 */
	private ArrayList<UUID> blockID = new ArrayList<UUID>();
	/** The height to spawn the <code>Meteorite</code> at. */
	private int initialLevel;

	/**
	 * Create a new <code>Meteorite</code> targeting the specified
	 * <code>Player</code>.
	 * 
	 * @param pl
	 *            the <code>MeteorMod</code> instance
	 * @param pT
	 *            the <code>Player</code> to target
	 * @param c
	 *            the time to display in the countdown in seconds
	 */
	public Meteorite(MeteorMod pl, Player pT, int c) {
		pTarget = pT;
		countdown = c;
		// module = pl;
		target = pTarget.getLocation();
		defaultMeteorite();
		Bukkit.getPluginManager().registerEvents(this, Sblock.getInstance());
		// see wall o' text below
		dropMeteorite();
	}

	/**
	 * Create a new <code>Meteorite</code> targeting a <code>Player</code> or
	 * <code>Location</code>.
	 * 
	 * @param instance
	 *            MeteorMod
	 * @param pT
	 *            the <code>Player</code>, <code>null</code> if
	 *            <code>Location</code> target
	 * @param xyz
	 *            the <code>Location</code> to drop the <code>Meteorite</code>
	 * @param m
	 *            the name of the <code>Material</code> to make the
	 *            <code>Meteorite</code> out of
	 * @param r
	 *            the radius to drop the <code>Meteorite</code> within
	 * @param c
	 *            the time to display to the <code>Player</code>, if any.
	 * @param explode
	 *            <code>true</code> if the <code>Meteorite</code> should explode
	 *            on contact with the ground
	 */
	public Meteorite(MeteorMod instance, Player pT, Location xyz, String m, int r, int c,
			boolean explode) {
		pTarget = pT;
		target = xyz;

		this.defaultMeteorite();

		if (r > 0) {
			radius = r;
		}
		if (!(m.equalsIgnoreCase(""))) {
			Material.matchMaterial(m);
		}
		if (c > 0 && pTarget != null) {
			countdown = c;
		}
		explosionBlockDamage = explode;
		// module = instance;
		Bukkit.getPluginManager().registerEvents(this, Sblock.getInstance());
		// Ok, I know this is pretty much the most awful practice ever, but I
		// need the list of meteorites in case
		// the UUID list doesn't end up fully empty for some reason - it would
		// suck to have like 43824578245 meteors
		// checking events. This is why I suggested exploding all UUIDs on
		// contact of 1. Would look less cool, yes,
		// but would prevent this sort of issue. Here we are instead, you're
		// welcome. ;-;
		dropMeteorite();
	}

	/**
	 * Generates the default meteorite properties.
	 */
	public void defaultMeteorite() {
		// target handled by constructor
		radius = DEFAULT_RADIUS;
		skyTarget = target.clone();
		target.setY(target.getWorld().getHighestBlockAt(target).getY());
		skyTarget.setY(255 - radius);
		mat = DEFAULT_MAT;
		countdown = DEFAULT_COUNTDOWN;
		if (pTarget != null)
			initialLevel = pTarget.getLevel();
	}

	/**
	 * The <code>EventHandler</code> for <code>EntityChangeBlockEvents</code> to
	 * handle <code>Meteorite</code> <code>FallingBlock</code> landings.
	 * 
	 * @param event
	 *            the <code>EntityChangeBlockEvent</code>
	 */
	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		// TODO should this use EntityFormBlockEvent instead?
		// Logger.getLogger("Minecraft").info("Meteor in position! (EntityChangeBlockEvent)");
		try {
			for (UUID u : blockID) {
				if (u.equals(event.getEntity().getUniqueId())) {
					// Logger.getLogger("Minecraft").info("Meteor in position!");
					explode(event.getBlock().getLocation());
					event.getBlock().setType(Material.AIR);
					blockID.remove(u);
					if (blockID.isEmpty())
						doHandlerUnregister();
					return;
				} else {
					event.setCancelled(true);
				}
			}
		} catch (NullPointerException e) {}
	}

	/**
	 * Drop the <code>Meteorite</code>, or, if there is still time remaining on
	 * the countdown, tick the timer.
	 */
	public void dropMeteorite() {
		if (countdown >= 0) {
			if (pTarget != null) {
				pTarget.setLevel(countdown);
			}
			doCounterTick();
			return;
		} else {
			if (pTarget != null) {
				pTarget.setLevel(initialLevel);
			}
			genMeteorite();
			if (sphereCoords.size() >= 1) { // Ensures that genSphereCoords has
											// been run (well)
				for (Location a : sphereCoords) {
					a.getBlock().setType(Material.AIR);
					blockID.add(skyTarget.getWorld().spawnFallingBlock(a, mat, (byte) 0)
							.getUniqueId());
				}
				Bukkit.getLogger().info(
						"Meteorificationalizing " + target.getBlockX() + ", " + target.getBlockZ());
			} else
				Bukkit.getLogger().info("What kind of a sphere did you just generate? Also, how?");
		}
	}

	/**
	 * Generates the <code>FallingBlock</code> sphere that represents a
	 * <code>Meteorite</code>.
	 */
	public void genMeteorite() {
		// Creates a floating ball. Calling this in dropMeteorite() to make life
		// easier.
		sphereCoords = this.genSphereCoords(radius);
		sphereCoords.removeAll(this.genSphereCoords(radius - 1));
		for (Location a : sphereCoords) {
			a.getBlock().setType(mat);
		}
	}

	/**
	 * Generates an <code>ArrayList</code> of the <code>Location</code>s in a
	 * hollow <code>Voxel</code> sphere around a <code>Location</code>.
	 * 
	 * @param r
	 *            the radius of the sphere
	 * @return the <code>ArrayList<Location></code> generated
	 */
	private ArrayList<Location> genSphereCoords(int r) {
		ArrayList<Location> coords = new ArrayList<Location>();
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
	 * Cause an explosion at a <code>Location</code>.
	 * 
	 * @param loc
	 *            the <code>Location</code> to explode at
	 */
	public void explode(Location loc) {
		loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4F, false,
				explosionBlockDamage);
	}

	/**
	 * Schedules a <code>ScheduledCounter</code> 1 second of game time (20
	 * ticks) later.
	 */
	public void doCounterTick() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(),
				new ScheduledCounter(), 20);
	}

	/**
	 * <code>Runnable</code> for ticking countdown and dropping a
	 * <code>Meteorite</code>.
	 */
	public class ScheduledCounter implements Runnable {

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			countdown--;
			dropMeteorite();
		}

	}

	/**
	 * Schedule unregistering this <code>Meteorite</code>'s
	 * <code>Listener</code>.
	 */
	public void doHandlerUnregister() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(),
				new HandlerUnregister(this));
	}

	/**
	 * <code>Runnable</code> for unregistering this <code>Meteorite</code>'s
	 * <code>Listener</code>.
	 */
	public class HandlerUnregister implements Runnable {
		/**
		 * the <code>Meteorite</code> to unregister the <code>Listener</code>
		 * of.
		 */
		Meteorite m;

		/**
		 * Constructor for handlerUnregister.
		 * 
		 * @param m
		 *            the <code>Meteorite</code>
		 */
		public HandlerUnregister(Meteorite m) {
			this.m = m;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			// I don't know if it's safe to do this inside an event handler, so
			// synchronous task.
			if (!blockID.isEmpty()) {
				// there is no easier way to get entity by UUID, sadly. Loop
				// here we come!
				for (Entity e : target.getWorld().getEntities()) {
					if (blockID.remove(e.getUniqueId())) {
						e.remove();
					}
				}
			}
			HandlerList.unregisterAll(m);
		}
	}
}
