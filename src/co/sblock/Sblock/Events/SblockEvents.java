package co.sblock.Sblock.Events;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Events.Listeners.*;
import co.sblock.Sblock.Events.Packets.SleepTeleport;
import co.sblock.Sblock.Events.Packets.WrapperPlayServerAnimation;
import co.sblock.Sblock.Events.Region.RegionCheck;
import co.sblock.Sblock.Events.Session.StatusCheck;
import co.sblock.Sblock.Events.Session.Status;
import co.sblock.Sblock.UserData.TowerData;
import co.sblock.Sblock.Utilities.Broadcast;
import co.sblock.Sblock.Utilities.Log;

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

	/** The Task ID of the RegionCheck task. */
	private int regionTask;

	/** The Task ID of the SessionCheck task. */
	private int sessionTask;

	/** The Minecraft servers' status */
	private Status status;

	/** A Map of all scheduled tasks by Player. */
	public Map<String, Integer> tasks;

	/** A Set of the names of all Players queuing to sleep teleport. */
	public Set<String> teleports;

	/**
	 * @see Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		tasks = new HashMap<String, Integer>();
		teleports = new HashSet<String>();
		towers = new TowerData();
		towers.load();
		
		this.registerEvents(new BlockBreakListener(), new BlockFadeListener(), new BlockGrowListener(),
				new BlockIgniteListener(), new BlockPhysicsListener(), new BlockPistonExtendListener(),
				new BlockPistonRetractListener(), new BlockPlaceListener(), new BlockSpreadListener(),

				new FurnaceBurnListener(), new InventoryClickListener(), new InventoryCloseListener(),
				new InventoryDragListener(), new InventoryMoveItemListener(),

				new PlayerAsyncChatListener(),
				new PlayerBedLeaveListener(), new PlayerChangedWorldListener(),
				new PlayerEditBookListener(), new PlayerInteractListener(),
				new PlayerJoinListener(), new PlayerLoginListener(), new PlayerQuitListener(),
				new PlayerTeleportListener(), new ServerListPingListener(),

				new SignChangeListener(), new VehicleBlockCollisionListener());
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
		towers.save();
		towers = null;
		instance = null;
	}

	/**
	 * Schedules a Player to be teleported to their DreamPlanet.
	 * 
	 * @param p the Player
	 */
	public void sleepDream(Player p) {
		tasks.put(p.getName(), Bukkit.getScheduler().scheduleSyncDelayedTask(
				Sblock.getInstance(), new SleepTeleport(p), 100L));
	}

	/**
	 * Sends a Player a fake packet for waking up.
	 * 
	 * @param p the Player
	 */
	public void fakeWakeUp(Player p) {
		WrapperPlayServerAnimation packet = new WrapperPlayServerAnimation();
		packet.setEntityID(p.getEntityId());
		packet.setAnimation((byte) WrapperPlayServerAnimation.Animations.LEAVE_BED);

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			Log.err(e);
		}
	}

	/**
	 * Schedules the SessionCheck to update the Status every minute.
	 * 
	 * @return the BukkitTask ID
	 */
	@SuppressWarnings("deprecation")
	private int initiateSessionChecks() {
		return Bukkit.getScheduler().scheduleAsyncRepeatingTask(Sblock.getInstance(), new StatusCheck(), 0L, 1200L);
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
	 * Gets the EventModule instance.
	 * 
	 * @return the EventModule instance.
	 */
	public static SblockEvents getEvents() {
		return instance;
	}
}
