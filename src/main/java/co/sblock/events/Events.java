package co.sblock.events;

import static org.bukkit.Material.ACTIVATOR_RAIL;
import static org.bukkit.Material.BARRIER;
import static org.bukkit.Material.BEACON;
import static org.bukkit.Material.BEDROCK;
import static org.bukkit.Material.BED_BLOCK;
import static org.bukkit.Material.BEETROOT_BLOCK;
import static org.bukkit.Material.BURNING_FURNACE;
import static org.bukkit.Material.CAKE_BLOCK;
import static org.bukkit.Material.CARROT;
import static org.bukkit.Material.COCOA;
import static org.bukkit.Material.COMMAND;
import static org.bukkit.Material.COMMAND_CHAIN;
import static org.bukkit.Material.COMMAND_MINECART;
import static org.bukkit.Material.COMMAND_REPEATING;
import static org.bukkit.Material.CROPS;
import static org.bukkit.Material.DAYLIGHT_DETECTOR_INVERTED;
import static org.bukkit.Material.DETECTOR_RAIL;
import static org.bukkit.Material.DIODE_BLOCK_OFF;
import static org.bukkit.Material.DIODE_BLOCK_ON;
import static org.bukkit.Material.DOUBLE_STEP;
import static org.bukkit.Material.DOUBLE_STONE_SLAB2;
import static org.bukkit.Material.ENDER_PORTAL;
import static org.bukkit.Material.ENDER_PORTAL_FRAME;
import static org.bukkit.Material.END_CRYSTAL;
import static org.bukkit.Material.END_GATEWAY;
import static org.bukkit.Material.EXPLOSIVE_MINECART;
import static org.bukkit.Material.FIRE;
import static org.bukkit.Material.FLOWER_POT;
import static org.bukkit.Material.HOPPER_MINECART;
import static org.bukkit.Material.IRON_DOOR_BLOCK;
import static org.bukkit.Material.JUKEBOX;
import static org.bukkit.Material.LAVA;
import static org.bukkit.Material.MELON_STEM;
import static org.bukkit.Material.MINECART;
import static org.bukkit.Material.MOB_SPAWNER;
import static org.bukkit.Material.MONSTER_EGG;
import static org.bukkit.Material.MONSTER_EGGS;
import static org.bukkit.Material.NETHER_WARTS;
import static org.bukkit.Material.PISTON_EXTENSION;
import static org.bukkit.Material.PISTON_MOVING_PIECE;
import static org.bukkit.Material.PORTAL;
import static org.bukkit.Material.POTATO;
import static org.bukkit.Material.POWERED_MINECART;
import static org.bukkit.Material.POWERED_RAIL;
import static org.bukkit.Material.PUMPKIN_STEM;
import static org.bukkit.Material.PURPUR_DOUBLE_SLAB;
import static org.bukkit.Material.RAILS;
import static org.bukkit.Material.REDSTONE_COMPARATOR_OFF;
import static org.bukkit.Material.REDSTONE_COMPARATOR_ON;
import static org.bukkit.Material.REDSTONE_LAMP_ON;
import static org.bukkit.Material.REDSTONE_TORCH_OFF;
import static org.bukkit.Material.SIGN_POST;
import static org.bukkit.Material.SKULL;
import static org.bukkit.Material.SOIL;
import static org.bukkit.Material.STANDING_BANNER;
import static org.bukkit.Material.STATIONARY_LAVA;
import static org.bukkit.Material.STATIONARY_WATER;
import static org.bukkit.Material.STORAGE_MINECART;
import static org.bukkit.Material.STRUCTURE_BLOCK;
import static org.bukkit.Material.STRUCTURE_VOID;
import static org.bukkit.Material.SUGAR_CANE_BLOCK;
import static org.bukkit.Material.TNT;
import static org.bukkit.Material.TRIPWIRE;
import static org.bukkit.Material.WALL_BANNER;
import static org.bukkit.Material.WALL_SIGN;
import static org.bukkit.Material.WATER;
import static org.bukkit.Material.WOODEN_DOOR;
import static org.bukkit.Material.WOOD_DOUBLE_STEP;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.Language;
import co.sblock.events.listeners.SblockListener;
import co.sblock.events.packets.SyncPacketAdapter;
import co.sblock.events.session.Status;
import co.sblock.events.session.StatusCheck;
import co.sblock.module.Module;
import co.sblock.utilities.TextUtils;

import com.comphenix.protocol.ProtocolLibrary;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import org.reflections.Reflections;

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

	public Events(Sblock plugin) {
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

		Reflections reflections = new Reflections("co.sblock.events.listeners");
		Set<Class<? extends SblockListener>> listeners = reflections.getSubTypesOf(SblockListener.class);
		for (Class<? extends SblockListener> listener : listeners) {
			if (!Sblock.areDependenciesPresent(listener)) {
				getLogger().info(listener.getSimpleName() + " dependencies not found.");
				continue;
			}
			try {
				Constructor<? extends SblockListener> constructor = listener.getConstructor(getPlugin().getClass());
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
