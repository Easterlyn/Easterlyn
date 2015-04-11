package co.sblock.events;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import org.reflections.Reflections;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.BlockPosition;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.events.listeners.plugin.CHBlockHealListener;
import co.sblock.events.packets.SleepTeleport;
import co.sblock.events.packets.SyncPacketAdapter;
import co.sblock.events.packets.WrapperPlayServerAnimation;
import co.sblock.events.packets.WrapperPlayServerBed;
import co.sblock.events.region.RegionCheck;
import co.sblock.events.session.Status;
import co.sblock.events.session.StatusCheck;
import co.sblock.module.Module;
import co.sblock.utilities.minecarts.FreeCart;

/**
 * The main Module for all events handled by the plugin.
 * 
 * @author Jikoo
 */
public class Events extends Module {

	private static Events instance;
	private Status status;
	private int statusResample = 0;
	private HashMap<UUID, BukkitTask> sleep;
	private LinkedHashMap<String, String> ipcache;
	private HashMap<UUID, BukkitTask> pvp;

	@Override
	protected void onEnable() {
		instance = this;
		sleep = new HashMap<>();
		pvp = new HashMap<>();

		ipcache = new LinkedHashMap<>();
		File file = new File(Sblock.getInstance().getDataFolder(), "ipcache.yml");
		if (file.exists()) {
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
			for (String ip : yaml.getKeys(false)) {
				ipcache.put(ip.replace("_", "."), yaml.getString(ip));
			}
		}

		status = Status.NEITHER;
		initiateSessionChecks();

		Reflections reflections = new Reflections("co.sblock.events.listeners");
		Set<Class<? extends Listener>> listeners = reflections.getSubTypesOf(Listener.class);
		for (Class<? extends Listener> listener : listeners) {
			if (listener.equals(CHBlockHealListener.class) && !Bukkit.getPluginManager().isPluginEnabled("CreeperHeal")) {
				getLogger().info("CreeperHeal not found, skipping listener.");
				continue;
			}
			try {
				Bukkit.getPluginManager().registerEvents(listener.newInstance(), Sblock.getInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				getLogger().severe("Unable to register events for " + listener.getName() + "!");
				e.printStackTrace();
			}
		}

		ProtocolLibrary.getProtocolManager().addPacketListener(new SyncPacketAdapter());

		//initiateRegionChecks();
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		FreeCart.getInstance().cleanUp();
		instance = null;

		try {
			File file = new File(Sblock.getInstance().getDataFolder(), "ipcache.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
			YamlConfiguration yaml = new YamlConfiguration();
			for (Entry<String, String> entry : ipcache.entrySet()) {
				yaml.set(entry.getKey().replace(".", "_"), entry.getValue());
			}
			yaml.save(file);
		} catch (IOException e) {
			getLogger().warning("Failed to save IP cache!");
			getLogger().err(e);
		}
	}

	/**
	 * Gets the HashMap of all SleepTeleports scheduled for players by UUID.
	 */
	public HashMap<UUID, BukkitTask> getSleepTasks() {
		return sleep;
	}

	/**
	 * Gets the HashMap PVP ending scheduled for players by UUID.
	 */
	public HashMap<UUID, BukkitTask> getPVPTasks() {
		return pvp;
	}

	/**
	 * Gets the player name stored for an IP.
	 */
	public String getIPName(String ip) {
		if (ipcache.containsKey(ip)) {
			return ipcache.get(ip);
		}
		return "Player";
	}

	/**
	 * Cache a player name for an IP. Resets cache position for existing IPs.
	 */
	public void addCachedIP(String ip, String name) {
		if (ipcache.containsKey(ip)) {
			// LinkedHashMaps replace the existing element, preserving order. We want latest logins last.
			ipcache.remove(ip);
		}
		ipcache.put(ip, name);
		// Clear oldest cached IPs
		int surplus = ipcache.size() - 1500;
		if (surplus < 1) {
			return;
		}
		for (Object entry : ipcache.entrySet().toArray()) {
			if (surplus <= 0) {
				break;
			}
			--surplus;

			ipcache.remove(((Entry<?, ?>) entry).getKey());
		}
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
		packet.setLocation(new BlockPosition(bed.getBlockX(), bed.getBlockY(), bed.getBlockZ()));

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			getLogger().err(e);
		}
		sleep.put(p.getUniqueId(), new SleepTeleport(p.getUniqueId()).runTaskLater(Sblock.getInstance(), 100L));
	}

	/**
	 * Sends a Player a fake packet for waking up.
	 * 
	 * @param p the Player
	 */
	public void fakeWakeUp(Player p) {
		WrapperPlayServerAnimation packet = new WrapperPlayServerAnimation();
		packet.setEntityId(p.getEntityId());
		packet.setAnimation(2); // http://wiki.vg/Protocol#Animation_2

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet.getHandle());
		} catch (InvocationTargetException e) {
			getLogger().err(e);
		}

		BukkitTask task = sleep.remove(p.getUniqueId());
		if (task != null) {
			task.cancel();
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
			Bukkit.broadcastMessage(ColorDef.HAL + announcement);
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
	public static Events getInstance() {
		return instance;
	}

	@Override
	protected String getModuleName() {
		return "Sblock Events";
	}
}
