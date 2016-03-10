package co.sblock.events;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import org.reflections.Reflections;

import com.google.common.collect.ImmutableList;

import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.listeners.SblockListener;
import co.sblock.events.packets.SyncPacketAdapter;
import co.sblock.events.session.Status;
import co.sblock.events.session.StatusCheck;
import co.sblock.module.Module;
import co.sblock.utilities.TextUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * The main Module for all events handled by the plugin.
 * 
 * @author Jikoo
 */
public class Events extends Module {

	private Status status;
	private int statusResample = 0;
	private final LinkedHashMap<String, String> ipcache;
	private final HashMap<UUID, BukkitTask> pvp;
	private final InvisibilityManager invisibilityManager;
	private final BlockUpdateManager blockUpdateManager;
	private final EnumSet<Material> creativeBlacklist;

	public Events(Sblock plugin) {
		super(plugin);
		this.pvp = new HashMap<>();
		this.ipcache = new LinkedHashMap<>();
		this.invisibilityManager = new InvisibilityManager(plugin);
		this.blockUpdateManager = new BlockUpdateManager(plugin);

		creativeBlacklist = EnumSet.of(Material.ACTIVATOR_RAIL, Material.BARRIER, Material.BEDROCK,
				Material.COMMAND, Material.COMMAND_CHAIN, Material.COMMAND_MINECART,
				Material.COMMAND_REPEATING, Material.DETECTOR_RAIL, Material.ENDER_PORTAL,
				Material.ENDER_PORTAL_FRAME, Material.EXPLOSIVE_MINECART, Material.HOPPER_MINECART,
				Material.JUKEBOX, Material.MINECART, Material.MOB_SPAWNER, Material.MONSTER_EGG,
				Material.MONSTER_EGGS, Material.POWERED_MINECART, Material.POWERED_RAIL,
				Material.RAILS, Material.STORAGE_MINECART, Material.TNT);
	}

	@Override
	protected void onEnable() {
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

	/**
	 * @see Module#onDisable()
	 */
	@Override
	protected void onDisable() {
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
			new MessageBuilder(getPlugin()).setSender(ChatColor.DARK_RED + getPlugin().getBotName())
					.setNameClick("/report ").setNameHover(ChatColor.RED + "Artifical Intelligence")
					.setChannel(getPlugin().getModule(Chat.class).getChannelManager().getChannel("#"))
					.setMessage(announcement).toMessage().send(Bukkit.getOnlinePlayers(), true, false);
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

	public InvisibilityManager getInvisibilityManager() {
		return invisibilityManager;
	}

	public BlockUpdateManager getBlockUpdateManager() {
		return blockUpdateManager;
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
