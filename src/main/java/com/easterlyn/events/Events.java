package com.easterlyn.events;

import com.comphenix.protocol.ProtocolLibrary;
import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.events.packets.SyncPacketAdapter;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
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

	public Events(Easterlyn plugin) {
		super(plugin);
		this.pvp = new HashMap<>();
		this.ipcache = new LinkedHashMap<>();
		this.invisibilityManager = new InvisibilityManager(plugin);
		this.blockUpdateManager = new BlockUpdateManager(plugin);
		this.spamhausWhitelist = new CopyOnWriteArrayList<>(this.getConfig().getStringList("spamWhitelist"));

		creativeBlacklist = EnumSet.of(BARRIER, BEACON, BEDROCK,
				COMMAND_BLOCK, CHAIN_COMMAND_BLOCK, COMMAND_BLOCK_MINECART, REPEATING_COMMAND_BLOCK,
				END_CRYSTAL, END_GATEWAY, END_PORTAL, END_PORTAL_FRAME, TNT_MINECART, FIRE, SPAWNER,
				NETHER_PORTAL, STRUCTURE_BLOCK, STRUCTURE_VOID, TNT);
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

		Reflections reflections = new Reflections("com.easterlyn.events.listeners");
		Set<Class<? extends EasterlynListener>> listeners = reflections.getSubTypesOf(EasterlynListener.class);
		for (Class<? extends EasterlynListener> listener : listeners) {
			if (Easterlyn.areDependenciesMissing(listener)) {
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
		this.blockUpdateManager.forceAllUpdates();

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
			// LinkedHashMaps replace the existing element, preserving order. We want latest logins last.
			ipcache.remove(ip);
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
