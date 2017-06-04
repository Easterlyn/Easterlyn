package com.easterlyn.events;

import com.comphenix.protocol.ProtocolLibrary;
import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.events.packets.SyncPacketAdapter;
import com.easterlyn.events.session.Status;
import com.easterlyn.events.session.StatusCheck;
import com.easterlyn.module.Module;
import com.easterlyn.utilities.TextUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.bukkit.Material.*;

/**
 * The main Module for all events handled by the plugin.
 * 
 * @author Jikoo
 */
public class Events extends Module {

	private final LinkedHashMap<String, String> ipcache;
	private final HashMap<UUID, BukkitTask> pvp;
	private final InvisibilityManager invisibilityManager;
	private final BlockUpdateManager blockUpdateManager;
	private final List<String> spamhausWhitelist;
	private final EnumSet<Material> creativeBlacklist;

	private Chat chat;
	private Status status;
	private int statusResample = 0;

	public Events(Easterlyn plugin) {
		super(plugin);
		this.pvp = new HashMap<>();
		this.ipcache = new LinkedHashMap<>();
		this.invisibilityManager = new InvisibilityManager(plugin);
		this.blockUpdateManager = new BlockUpdateManager(plugin);
		this.spamhausWhitelist = new CopyOnWriteArrayList<>(this.getConfig().getStringList("spamWhitelist"));

		creativeBlacklist = EnumSet.of(ACTIVATOR_RAIL, BARRIER, BEACON, BED_BLOCK, BEDROCK,
				BEETROOT_BLOCK, BURNING_FURNACE, CAKE_BLOCK, CARROT, COCOA, COMMAND, COMMAND_CHAIN,
				COMMAND_MINECART, COMMAND_REPEATING, CROPS, DAYLIGHT_DETECTOR_INVERTED,
				DETECTOR_RAIL, DIODE_BLOCK_OFF, DIODE_BLOCK_ON, DOUBLE_STEP, DOUBLE_STONE_SLAB2,
				PURPUR_DOUBLE_SLAB, END_CRYSTAL, END_GATEWAY, ENDER_PORTAL, ENDER_PORTAL_FRAME,
				EXPLOSIVE_MINECART, FIRE, FLOWER_POT, HOPPER_MINECART, IRON_DOOR_BLOCK, JUKEBOX,
				LAVA, MELON_STEM, MINECART, MOB_SPAWNER, MONSTER_EGG, MONSTER_EGGS, NETHER_WARTS,
				PISTON_EXTENSION, PISTON_MOVING_PIECE, PORTAL, POTATO, POWERED_MINECART,
				POWERED_RAIL, PUMPKIN_STEM, RAILS, REDSTONE_COMPARATOR_OFF, REDSTONE_COMPARATOR_ON,
				REDSTONE_LAMP_ON, REDSTONE_TORCH_OFF, SIGN_POST, SKULL, SOIL, STANDING_BANNER,
				STATIONARY_LAVA, STATIONARY_WATER, STORAGE_MINECART, STRUCTURE_BLOCK,
				STRUCTURE_VOID, SUGAR_CANE_BLOCK, TNT, TRIPWIRE, WALL_BANNER, WALL_SIGN, WATER,
				WOOD_DOUBLE_STEP, WOODEN_DOOR);
	}

	@Override
	protected void onEnable() {
		this.chat = this.getPlugin().getModule(Chat.class);
		File file = new File(getPlugin().getDataFolder(), "ipcache.yml");
		if (file.exists()) {
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
			for (String ip : yaml.getKeys(false)) {
				ipcache.put(ip.replace("_", "."), yaml.getString(ip));
			}
		}

		status = Status.NEITHER;
		new StatusCheck().runTaskTimerAsynchronously(getPlugin(), 100L, 1200L);

		Reflections reflections = new Reflections("com.easterlyn.events.listeners");
		Set<Class<? extends EasterlynListener>> listeners = reflections.getSubTypesOf(EasterlynListener.class);
		for (Class<? extends EasterlynListener> listener : listeners) {
			if (!Easterlyn.areDependenciesPresent(listener)) {
				getLogger().info(listener.getSimpleName() + " dependencies not found.");
				continue;
			}
			try {
				Constructor<? extends EasterlynListener> constructor = listener.getConstructor(getPlugin().getClass());
				Bukkit.getPluginManager().registerEvents(constructor.newInstance(getPlugin()), getPlugin());
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
					| SecurityException | IllegalArgumentException | InvocationTargetException e) {
				getLogger().severe("Unable to register events for " + listener.getName() + "!");
				e.printStackTrace();
			}
		}

		ProtocolLibrary.getProtocolManager().addPacketListener(new SyncPacketAdapter(getPlugin()));
	}

	@Override
	protected void onDisable() {
		this.getConfig().set("spamWhitelist", spamhausWhitelist);

		try {
			File file = new File(getPlugin().getDataFolder(), "ipcache.yml");
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
			getLogger().warning(TextUtils.getTrace(e));
		}
	}

	/**
	 * Gets the HashMap PVP ending scheduled for players by UUID.
	 */
	public HashMap<UUID, BukkitTask> getPVPTasks() {
		return pvp;
	}

	/**
	 * Gets the creative material blacklist.
	 */
	public Set<Material> getCreativeBlacklist() {
		return this.creativeBlacklist;
	}

	/**
	 * Gets the player name stored for an IP.
	 */
	public String getIPName(String ip) {
		synchronized (ipcache) {
			if (ipcache.containsKey(ip)) {
				return ipcache.get(ip);
			}
			return "Player";
		}
	}

	/**
	 * Cache a player name for an IP. Resets cache position for existing IPs.
	 */
	public void addCachedIP(String ip, String name) {
		synchronized (ipcache) {
			if (ipcache.containsKey(ip)) {
				// LinkedHashMaps replace the existing element, preserving order. We want latest logins last.
				ipcache.remove(ip);
			}
			ipcache.put(ip, name);

			// Clear oldest cached IPs
			int surplus = ipcache.size() - 1500;
			for (Iterator<Entry<String, String>> entryIterator = ipcache.entrySet().iterator();
					entryIterator.hasNext() && surplus > 0; --surplus) {
				entryIterator.remove();
			}
		}
	}

	/**
	 * Get all cached IPs for a UUID.
	 * 
	 * @param uuid the UUID
	 * 
	 * @return a Collection of all matching IPs.
	 */
	public Collection<String> getIPsFor(UUID uuid) {
		OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
		if (!offline.hasPlayedBefore()) {
			return ImmutableList.of();
		}
		return getIPsFor(offline.getName());
	}

	/**
	 * Get all cached IPs for a name.
	 * 
	 * @param name the name
	 * 
	 * @return a Collection of all matching IPs.
	 */
	public Collection<String> getIPsFor(String name) {
		synchronized (ipcache) {
			ArrayList<String> list = new ArrayList<>();
			for (Map.Entry<String, String> entry : ipcache.entrySet()) {
				if (entry.getValue().equals(name)) {
					list.add(entry.getKey());
				}
			}
			return list;
		}
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
		if (status == this.status) {
			statusResample = 0;
			return;
		}
		if (statusResample < 5) {
			// less spam - must have status change 5 times in a row to announce.
			statusResample++;
			new StatusCheck().runTaskLaterAsynchronously(getPlugin(), 50L);
			return;
		}
		String announcement = null;
		if (status.hasAnnouncement()) {
			announcement = status.getAnnouncement();
		} else {
			announcement = this.status.getAllClear();
		}
		if (announcement != null) {
			this.chat.getHalBase().setMessage(Language.getColor("bot_text") + announcement)
					.toMessage().send(Bukkit.getOnlinePlayers(), true, false);
			this.statusResample = 0;
		}
		this.status = status;
	}

	/**
	 * Gets the current Status.
	 */
	public Status getStatus() {
		return status;
	}

	public InvisibilityManager getInvisibilityManager() {
		return invisibilityManager;
	}

	public BlockUpdateManager getBlockUpdateManager() {
		return blockUpdateManager;
	}

	public List<String> getSpamhausWhitelist() {
		return spamhausWhitelist;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public String getName() {
		return "Events";
	}

}
