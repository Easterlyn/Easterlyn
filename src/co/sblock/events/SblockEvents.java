package co.sblock.events;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Module;
import co.sblock.Sblock;
import co.sblock.events.listeners.*;
import co.sblock.events.packets.SleepTeleport;
import co.sblock.events.packets.WrapperPlayServerAnimation;
import co.sblock.events.packets.WrapperPlayServerBed;
import co.sblock.events.region.RegionCheck;
import co.sblock.events.session.Status;
import co.sblock.events.session.StatusCheck;
import co.sblock.users.TowerData;
import co.sblock.utilities.Broadcast;

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

	/** The number of repeated status checks that have come back red. */
	private int statusResample = 0;

	/** A Map of all scheduled tasks by Player. */
	public Map<String, Integer> tasks;

	/** A Set of the names of all Players queuing to sleep teleport. */
	public Set<String> teleports;

	/** A Set of the names of all Players opening Captchadexes. */
	public Set<String> openingCaptchadex;

	/**
	 * @see Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		tasks = new HashMap<String, Integer>();
		teleports = new HashSet<String>();
		openingCaptchadex = new HashSet<String>();
		towers = new TowerData();
		towers.load();
		
		this.registerEvents(new BlockBreakListener(), new BlockFadeListener(), new BlockGrowListener(),
				new BlockIgniteListener(), new BlockPhysicsListener(), new BlockPistonExtendListener(),
				new BlockPistonRetractListener(), new BlockPlaceListener(), new BlockSpreadListener(),

				new EntityDamageByEntityListener(), new EntityDamageListener(),
				// Temporarily disabling explosion dodging in favor of blocking places in Machine locations.
				// new EntityExplodeListener(),
				new EntityRegainHealthListener(), new FoodLevelChangeListener(),
				
				new FurnaceBurnListener(), new FurnaceSmeltListener(),

				new InventoryClickListener(), new InventoryCloseListener(),
				new InventoryDragListener(), new InventoryMoveItemListener(),
				new InventoryOpenListener(),

				new PlayerAsyncChatListener(), new PlayerChangedWorldListener(),
				new PlayerCommandPreprocessListener(), new PlayerDeathListener(),
				new PlayerDropItemListener(), new PlayerEditBookListener(),
				new PlayerInteractEntityListener(), new PlayerInteractListener(),
				new PlayerItemHeldListener(),
				new PlayerJoinListener(), new PlayerLoginListener(),
				new PlayerPickupItemListener(), new PlayerQuitListener(),
				new PlayerTeleportListener(), new ServerListPingListener(),

				new SignChangeListener(), new VehicleBlockCollisionListener());
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener());
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
	 * Sends a Player a fake packet for starting sleeping and schedules them to
	 * be teleported to their DreamPlanet.
	 * 
	 * @param p the Player
	 * @param bed the Location of the bed to sleep in
	 */
	public void fakeSleepDream(Player p, Location bed) {

		WrapperPlayServerBed packet = new WrapperPlayServerBed();
		packet.setEntityId(p.getEntityId());
		packet.setX(bed.getBlockX());
		packet.setY((byte) bed.getBlockY());
		packet.setZ(bed.getBlockZ());

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			getLogger().err(e);
		}
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
			getLogger().err(e);
		}

		Integer taskID = tasks.remove(p.getName());
		if (taskID != null) {
			Bukkit.getScheduler().cancelTask(taskID);
		}
	}

	/**
	 * Schedules the SessionCheck to update the Status every minute.
	 * 
	 * @return the BukkitTask ID
	 */
	@SuppressWarnings("deprecation")
	private int initiateSessionChecks() {
		return Bukkit.getScheduler().scheduleAsyncRepeatingTask(Sblock.getInstance(), new StatusCheck(), 200L, 1200L);
	}

	/**
	 * Change the Status of Minecraft's servers.
	 * <p>
	 * If a service is down, this will announce the issue to all players and set
	 * a relevant MOTD.
	 * 
	 * @param status the Status
	 */
	@SuppressWarnings("deprecation")
	public void changeStatus(Status status) {
		if (status.hasAnnouncement() && statusResample < 5) {
			// less spam - must return red status 5 times in a row to announce.
			statusResample++;
			Bukkit.getScheduler().scheduleAsyncDelayedTask(Sblock.getInstance(), new StatusCheck());
			return;
		}
		String announcement = null;
		if (this.status.hasAllClear() && !status.hasAnnouncement()) {
			announcement = this.status.getAllClear();
		} else if (status.hasAnnouncement() && status != this.status) {
			announcement = status.getAnnouncement();
		}
		if (announcement != null) {
			Broadcast.lilHal(announcement);
			statusResample = 0;
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
