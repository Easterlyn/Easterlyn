package co.sblock.events;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Sblock;
import co.sblock.events.listeners.*;
import co.sblock.events.packets.SleepTeleport;
import co.sblock.events.packets.WrapperPlayServerAnimation;
import co.sblock.events.packets.WrapperPlayServerBed;
import co.sblock.events.region.RegionCheck;
import co.sblock.events.session.Status;
import co.sblock.events.session.StatusCheck;
import co.sblock.module.Module;
import co.sblock.utilities.Broadcast;
import co.sblock.utilities.minecarts.FreeCart;

/**
 * The main Module for all events handled by the plugin.
 * 
 * @author Jikoo
 */
public class SblockEvents extends Module {

	/* The EventModule instance. */
	private static SblockEvents instance;

	/* The Minecraft servers' status */
	private Status status;

	/* The number of repeated status checks that have come back red. */
	private int statusResample = 0;

	/* A Map of all scheduled tasks by Player. */
	public Map<String, Integer> tasks;

	/* A Set of the names of all Players queuing to sleep teleport. */
	public Set<String> teleports;

	/* A Set of the names of all Players opening Captchadexes. */
	public Set<String> openingCaptchadex;

	/* The time at which SblockEvents was enabled (generally server start) */
	private long start;

	/* Boolean representing whether or not the server should restart next chance. */
	private boolean restart;

	@Override
	protected void onEnable() {
		instance = this;
		tasks = new HashMap<String, Integer>();
		teleports = new HashSet<String>();
		openingCaptchadex = new HashSet<String>();
		start = System.currentTimeMillis();
		restart = false;

		status = Status.NEITHER;
		initiateSessionChecks();

		this.registerEvents(new BlockBreakListener(), new BlockFadeListener(), new BlockGrowListener(),
				new BlockIgniteListener(), new BlockPhysicsListener(), new BlockPistonExtendListener(),
				new BlockPistonRetractListener(), new BlockPlaceListener(), new BlockSpreadListener(),

				new CraftItemListener(),

				new EntityDamageByEntityListener(), new EntityDamageListener(), new EntityExplodeListener(),
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
				new PlayerTeleportListener(), new PrepareItemEnchantListener(),
				new ServerListPingListener(),

				new SignChangeListener(),
				
				new HorseMountListener(),

				new VehicleBlockCollisionListener(), new VehicleDestroyListener(), new VehicleExitListener());

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener());
		ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new AsyncPacketAdapter()).start(4);

		if (Bukkit.getPluginManager().isPluginEnabled("CreeperHeal")) {
			this.registerEvents(new CHBlockHealListener());
		}

		initiateRegionChecks();
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		FreeCart.getInstance().cleanUp();
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
	 * Schedules a SessionCheck to update the Status every minute.
	 */
	private void initiateSessionChecks() {
		new StatusCheck().runTaskTimerAsynchronously(Sblock.getInstance(), 100L, 1200L);
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
		if (status.hasAnnouncement() && statusResample < 5) {
			// less spam - must return red status 5 times in a row to announce.
			statusResample++;
			new StatusCheck().runTaskAsynchronously(Sblock.getInstance());
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
	 */
	public void initiateRegionChecks() {
		new RegionCheck().runTaskTimer(Sblock.getInstance(), 100L, 100L);
	}

	/**
	 * Gets the EventModule instance.
	 * 
	 * @return the EventModule instance.
	 */
	public static SblockEvents getEvents() {
		return instance;
	}

	@Override
	protected String getModuleName() {
		return "EventsModule";
	}

	public void setSoftRestart(boolean restart) {
		this.restart = restart;
	}

	public boolean getSoftRestart() {
		return restart;
	}

	public boolean recalculateRestart() { // TODO task to trigger this check
		if (!restart) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			// 20h * 60min * 60s * 1000ms = 72000000, 3600000 = 1h
			restart = start - System.currentTimeMillis() > 72000000
					|| start - System.currentTimeMillis() > 10800000 && calendar.get(Calendar.HOUR_OF_DAY) < 3;
		}
		return restart;
	}
}
