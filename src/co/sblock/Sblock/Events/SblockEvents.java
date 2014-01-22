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
 * The main Module for all events handled by the plugin.
 * 
 * @author Jikoo
 */
public class SblockEvents extends Module {

	/** The EventModule instance. */
	private static SblockEvents instance;

	/** The TowerData. */
	private TowerData towers;

	/** The EventListener. */
	private EventListener listener;

	/** The Task ID of the RegionCheck task. */
	private int regionTask;

	/** The Task ID of the SessionCheck task. */
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
	 * Schedules the SessionCheck to update the Status every minute.
	 * 
	 * @return the BukkitTask ID
	 */
	@SuppressWarnings("deprecation")
	private int initiateSessionChecks() {
		return Bukkit.getScheduler().scheduleAsyncRepeatingTask(Sblock.getInstance(), new SessionCheck(), 0L, 1200L);
	}

	/**
	 * Change the Status of Minecraft's servers.
	 * <p>
	 * If a service is down, this will announce the issue to all players and set
	 * a relevant MOTD.
	 * 
	 * @param status the Status
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
	 * Gets the current Status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Schedules the RegionCheck to update the Region for each Player online
	 * every 5 seconds of game time (100 ticks).
	 * 
	 * @return the BukkitTask ID
	 */
	public int initiateRegionChecks() {
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new RegionCheck(), 0L, 100L);
	}

	/**
	 * Gets the TowerData used for sleep teleports.
	 * 
	 * @return the TowerData
	 */
	public TowerData getTowerData() {
		return towers;
	}

	/**
	 * Gets the EventListener.
	 * 
	 * @return the EventListener
	 */
	public EventListener getListener() {
		return this.listener;
	}

	/**
	 * Gets the EventModule instance.
	 * 
	 * @return the EventModule instance.
	 */
	public static SblockEvents getEvents() {
		return instance;
	}
}
