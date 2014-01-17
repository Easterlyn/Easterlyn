package co.sblock.Sblock.Events;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Events.Region.RegionCheck;
import co.sblock.Sblock.Events.Session.SessionCheck;
import co.sblock.Sblock.Events.Session.Status;
import co.sblock.Sblock.UserData.TowerData;
import co.sblock.Sblock.Utilities.Broadcast;

/**
 * @author Jikoo
 */
public class SblockEvents extends Module {

	/** The <code>EventModule</code> instance. */
	private static SblockEvents instance;
	/** The <code>TowerData</code>. */
	private TowerData towers;
	/** The <code>EventListener</code>. */
	private EventListener listener;
	/** The Task ID of the <code>RegionCheck</code> task. */
	private int regionTask;
	/** The Task ID of the <code>SessionCheck</code> task. */
	private int sessionTask;
	/** The Minecraft servers' status */
	private Status status;

	/**
	 * @see Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		towers = new TowerData();
		towers.load();
		listener = new EventListener();
		this.registerEvents(listener);
		ProtocolLibrary.getProtocolManager().addPacketListener(listener);
		status = Status.NEITHER;
		regionTask = initiateRegionChecks();
		sessionTask = initiateSessionChecks();
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		Bukkit.getScheduler().cancelTask(regionTask);
		Bukkit.getScheduler().cancelTask(sessionTask);
		HandlerList.unregisterAll(listener);
		listener = null;
		towers.save();
		towers = null;
		instance = null;
	}

	/**
	 * Schedules the <code>SessionCheck</code> to update the <code>Status</code> every 5 minutes.
	 * 
	 * @return the <code>BukkitTask</code> ID
	 */
	@SuppressWarnings("deprecation")
	private int initiateSessionChecks() {
		return Bukkit.getScheduler().scheduleAsyncRepeatingTask(Sblock.getInstance(), new SessionCheck(), 0L, 1200L);
	}

	/**
	 * Change the <code>Status</code> of Minecraft's servers.
	 * <p>
	 * If a service is down, this will announce the issue to all players and set
	 * a relevant MOTD.
	 * 
	 * @param status
	 *            the <code>Status</code>
	 */
	public void changeStatus(Status status) {
		String announcement = null;
		if (this.status.hasAllClear() && !status.hasAnnouncement()) {
			announcement = this.status.getAllClear();
		} else if (status.hasAnnouncement()) {
			announcement = status.getAnnouncement();
		}
		if (announcement != null) {
			Broadcast.lilHal(announcement);
		}
		this.status = status;
	}

	/**
	 * Gets the current <code>Status</code>.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Schedules the <code>RegionCheck</code> to update the <code>Region</code>
	 * for each <code>Player</code> online every 5 seconds of game time (100
	 * ticks).
	 * 
	 * @return the <code>BukkitTask</code> ID
	 */
	public int initiateRegionChecks() {
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new RegionCheck(), 0L, 100L);
	}

	/**
	 * Gets the <code>TowerData</code> used for sleep teleports.
	 * 
	 * @return the <code>TowerData</code>
	 */
	public TowerData getTowerData() {
		return towers;
	}

	/**
	 * Gets the <code>EventListener</code>.
	 * 
	 * @return the <code>EventListener</code>
	 */
	public EventListener getListener() {
		return this.listener;
	}

	/**
	 * Gets the <code>EventModule</code> instance.
	 * 
	 * @return the <code>EventModule</code> instance.
	 */
	public static SblockEvents getEvents() {
		return instance;
	}
}
