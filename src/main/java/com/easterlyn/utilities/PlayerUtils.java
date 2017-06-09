package com.easterlyn.utilities;

import com.easterlyn.Easterlyn;
import com.easterlyn.discord.Discord;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Utility for accessing Players.
 *
 * @author Jikoo
 */
public class PlayerUtils {

	private static final Cache<UUID, Player> PLAYER_CACHE = CacheBuilder.newBuilder().weakValues()
			.expireAfterAccess(5, TimeUnit.MINUTES).maximumSize(50).build();

	/**
	 * Get a Player for the specified UUID if they have logged in in the past, even if offline.
	 *
	 * @param plugin the Plugin instance used to schedule a task. Can be null if called on the main thread
	 * @param uuid the UUID
	 *
	 * @return the Player, or null if they have not logged in
	 * @throws IllegalStateException if passed a null Plugin and called off the main thread
	 */
	public static Player getPlayer(Plugin plugin, UUID uuid) {
		return getPlayer(plugin, uuid, true);
	}

	/**
	 * Get a Player for the specified UUID if they have logged in in the past, even if offline.
	 *
	 * @param plugin the Plugin instance used to schedule a task. Can be null if called on the main thread
	 * @param uuid the UUID
	 * @param useCached true if the Player cache is preferred over loading a new Player
	 *
	 * @return the Player, or null if they have not logged in
	 * @throws IllegalStateException if passed a null Plugin and called off the main thread
	 */
	public static Player getPlayer(Plugin plugin, UUID uuid, boolean useCached) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			// Online, life is easy.
			return player;
		}

		if (useCached) {
			player = PLAYER_CACHE.getIfPresent(uuid);
			if (player != null) {
				return player;
			}
		} else {
			// Invalidate cached player in case new player loaded is modified
			PLAYER_CACHE.invalidate(uuid);
		}

		if (Bukkit.isPrimaryThread()) {
			return getPlayerFor(uuid);
		} else if (plugin == null) {
			throw new IllegalStateException("Asynchronous player load must use PlayerUtils#getPlayer(Plugin, UUID)");
		}
		Future<Player> future = Bukkit.getScheduler().callSyncMethod(plugin, () -> getPlayerFor(uuid));

		int ticks = 0;
		while (!future.isDone() && !future.isCancelled() && ticks < 10) {
			++ticks;
			try {
				Thread.sleep(50L);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
		if (!future.isDone() || future.isCancelled()) {
			return null;
		}
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Player getPlayerFor(UUID uuid) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		if (offlinePlayer == null || offlinePlayer.getName() == null) {
			// Player has not logged in.
			return null;
		}
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		EntityPlayer nmsPlayer = new EntityPlayer(server, server.getWorldServer(0),
				new GameProfile(uuid, offlinePlayer.getName()),
				new PlayerInteractManager(server.getWorldServer(0)));

		Player player = nmsPlayer.getBukkitEntity();
		if (player == null) {
			return null;
		}
		if (offlinePlayer.hasPlayedBefore()) {
			player.loadData();
		}
		// Wrap player so permissions checks will work
		player = new PermissiblePlayer(player);
		PLAYER_CACHE.put(uuid, player);
		return player;
	}

	public static void modifyCachedPlayer(Player player) {
		PLAYER_CACHE.put(player.getUniqueId(), player);
	}

	/**
	 * Matches an online Player for a given sender Player.
	 *
	 * @param sender the sender, or null to ignore visibility
	 * @param id the identifier used to match a Player
	 * @return the Player, or null if no matches were found
	 */
	public static Player matchOnlinePlayer(CommandSender sender, String id) {

		Player senderPlayer = sender != null && sender instanceof Player ? (Player) sender : null;

		try {
			UUID uuid = UUID.fromString(id);
			Player player = Bukkit.getPlayer(uuid);
			if (player == null || senderPlayer == null || senderPlayer.canSee(player)) {
				return player;
			}
			return null;
		} catch (IllegalArgumentException e) {
			// Not a UUID.
		}

		// Ensure valid name.
		if (Bukkit.getOnlineMode() && !id.matches("[a-zA-Z0-9_]{3,16}")) {
			return null;
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (senderPlayer != null && !senderPlayer.canSee(player)) {
				continue;
			}

			if (StringUtil.startsWithIgnoreCase(player.getName(), id)
					|| player.getDisplayName() != null && StringUtil.startsWithIgnoreCase(player.getDisplayName(), id)) {
				return player;
			}
		}

		return null;
	}

	/**
	 * Match a Player, online or off. If matching offline players, do not call on the main thread!
	 *
	 * @param id the UUID or name of the Player
	 * @param offline true if offline Players should be matched
	 * @param plugin the Plugin instance
	 * @return the Player, or null if no matches were found
	 */
	@SuppressWarnings("deprecation")
	public static Player matchPlayer(String id, boolean offline, Plugin plugin) {
		// TODO: nick support
		// Warn if called on the main thread - if we resort to searching offline players, this may take several seconds.
		if (Bukkit.isPrimaryThread() && offline) {
			((Easterlyn) Bukkit.getPluginManager().getPlugin("Easterlyn")).getModule(Discord.class).postReport(
					"Adam has been bad. This report has been generated to ~~shame him about~~ alert him of an issue."
							+ "\nCalled PlayerUtils#matchPlayer matching offline players on the main thread."
							+ "\nTrace:\n" + TextUtils.getTrace(new Throwable().fillInStackTrace(), 5));
		}

		try {
			UUID uuid = UUID.fromString(id);

			if (!offline) {
				return Bukkit.getPlayer(uuid);
			}

			return getPlayer(plugin, uuid);
		} catch (IllegalArgumentException e) {
			// Not a UUID.
		}

		// Ensure valid name.
		if (Bukkit.getOnlineMode() && !id.matches("[a-zA-Z0-9_]{3,16}")) {
			return null;
		}

		// Exact online match
		OfflinePlayer player = Bukkit.getPlayerExact(id);

		if (player != null) {
			return player.getPlayer();
		}

		if (offline) {
			// Exact offline match from usercache
			player = Bukkit.getOfflinePlayer(id);
			if (player != null && player.hasPlayedBefore()) {
				return getPlayer(plugin, player.getUniqueId());
			}
		}

		// Inexact online match
		player = Bukkit.getPlayer(id);

		if (player != null) {
			return player.getPlayer();
		}

		if (!offline) {
			return null;
		}

		int bestMatch = Integer.MAX_VALUE;
		for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
			if (offlinePlayer.getName() == null) {
				// Loaded by UUID only, name has never been looked up.
				continue;
			}

			int currentMatch = StringUtils.getLevenshteinDistance(id, offlinePlayer.getName());

			if (currentMatch == 0) {
				player = offlinePlayer;
				break;
			}

			if (currentMatch < bestMatch) {
				bestMatch = currentMatch;
				player = offlinePlayer;
			}
		}

		// Only null if no players have played ever, otherwise even the worst match will do.
		if (player == null) {
			return null;
		}

		return getPlayer(plugin, player.getUniqueId());
	}

}
