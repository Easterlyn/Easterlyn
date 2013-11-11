package co.sblock.Sblock.Utilities.MeteorMod;

import java.util.ArrayList;

import org.bukkit.event.Listener;
import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;

/**
 * @author Dublek
 */
public class MeteorMod extends Module implements Listener {
	/** The MeteorMod instance. */
	private static MeteorMod instance;
	// private BukkitTask task;
	/** The MeteorCommandListener. */
	private MeteorCommandListener mcl = new MeteorCommandListener();

	/** The <code>List</code> of active <code>Meteorite</code>s. */
	private static ArrayList<Meteorite> meteorites = new ArrayList<Meteorite>();

	// keiko Move commands into Fire's command handler
	/**
	 * @see Module#onEnable()
	 */
	@Override
	public void onEnable() {
		this.registerCommands(mcl);
		Sblock.getInstance().getServer().getPluginManager()
				.registerEvents(this, Sblock.getInstance());
		// startReckoning(20*20);
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	public void onDisable() {
		meteorites = null;
		// stopReckoning();
	}

	/**
	 * Gets the <code>MeteorMod</code> instance.
	 * 
	 * @return the <code>MeteorMod</code> instance
	 */
	public static MeteorMod getInstance() {
		return instance;
	}

	/**
	 * Gets the <code>List</code> of <code>Meteorite</code>s currently active.
	 * 
	 * @return the <code>ArrayList<Meteorite></code>
	 */
	public static ArrayList<Meteorite> getMeteorites() {
		return meteorites;
	}

	/**
	 * Starts <code>Meteorite</code>s being created in the area of online
	 * <code>Players</code>.
	 * 
	 * @param rLong
	 *            the time to delay the reckoning start by in ticks
	 */
	/*
	 * public void startReckoning(long rLong) { task = new
	 * scheduledReckoning().runTaskTimer(this, 20*300, rLong); }
	 * 
	 * public void stopReckoning() { task.cancel(); }
	 */

	/*
	 * public class scheduledReckoning extends BukkitRunnable {
	 * 
	 * @Override public void run() { if (getServer().getOnlinePlayers().length
	 * >= 1) { Player pTarget = getServer().getOnlinePlayers()[(int)
	 * (getServer() .getOnlinePlayers().length * Math.random())]; Location
	 * target = pTarget.getLocation(); target.setX((int) ((160 * Math.random())
	 * - 80)); target.setZ((int) ((160 * Math.random()) - 80)); int radius = -1;
	 * int countdown = -1; String material = ""; boolean blockDamage = false;
	 * getLogger().info( pTarget.getName() +
	 * "has been randomly selected for termination"); meteorites.add(new
	 * Meteorite(plugin, pTarget, target, material, radius, countdown,
	 * blockDamage)); } } }
	 */
}
