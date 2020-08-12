package com.easterlyn.util;

import com.easterlyn.user.UserRank;
import com.easterlyn.util.wrapper.PermissiblePlayer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.MinecraftServer;
import net.minecraft.server.v1_16_R2.PlayerInteractManager;
import net.minecraft.server.v1_16_R2.World;
import net.minecraft.server.v1_16_R2.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility for accessing Players.
 *
 * @author Jikoo
 */
public class PlayerUtil {

	private static final Cache<UUID, Player> PLAYER_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES).maximumSize(50)
			.removalListener(notification -> {
				if (notification.getValue() == null) {
					return;
				}
				Player player = (Player) notification.getValue();
				// Save if over 45 days since last login, removes achievements that should not be present.
				if (!player.isOnline() && player.getLastPlayed() < System.currentTimeMillis() - 3888000000L) {
					player.saveData();
				}
			}).build();

	static {
		PermissionUtil.addParent("easterlyn.command.selector", UserRank.STAFF.getPermission());
	}

	/**
	 * Removes a cached Player if present.
	 *
	 * @param uuid the UUID of the Player.
	 */
	public static void removeFromCache(@NotNull UUID uuid) {
		PLAYER_CACHE.invalidate(uuid);
	}

	/**
	 * Get a Player for the specified UUID if they have logged in in the past, even if offline.
	 *
	 * @param plugin the Plugin instance used to schedule a task. Can be null if called on the main thread
	 * @param uuid the UUID
	 *
	 * @return the Player, or null if they have not logged in
	 * @throws IllegalAccessException if passed a null Plugin and called off the main thread
	 */
	@Nullable
	public static Player getPlayer(@Nullable Plugin plugin, @NotNull UUID uuid) throws IllegalAccessException {
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
	 * @throws IllegalAccessException if passed a null Plugin and called off the main thread
	 */
	@Nullable
	public static Player getPlayer(@Nullable Plugin plugin, @NotNull UUID uuid, boolean useCached) throws IllegalAccessException {
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
			throw new IllegalAccessException("Asynchronous player load must use PlayerUtils#getPlayer(Plugin, UUID)");
		}

		try {
			return Bukkit.getScheduler().callSyncMethod(plugin, () -> getPlayerFor(uuid)).get(1, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Nullable
	private static Player getPlayerFor(@NotNull UUID uuid) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		if (offlinePlayer.getName() == null) {
			// Player has not logged in.
			return null;
		}
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		WorldServer worldServer = server.getWorldServer(World.OVERWORLD);

		if (worldServer == null) {
			return null;
		}

		EntityPlayer nmsPlayer = new EntityPlayer(server, worldServer,
				new GameProfile(uuid, offlinePlayer.getName()),
				new PlayerInteractManager(worldServer));
		// TODO: swap to OpenInv to prevent overwriting mounts?

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

	public static void modifyCachedPlayer(@NotNull Player player) {
		PLAYER_CACHE.put(player.getUniqueId(), player);
	}

	/**
	 * Matches an online Player for a given sender Player.
	 *
	 * @param sender the sender, or null to ignore visibility
	 * @param id the identifier used to match a Player
	 * @return the Player, or null if no matches were found
	 */
	@Nullable
	public static Player matchOnlinePlayer(@Nullable CommandSender sender, @NotNull String id) {

		Player senderPlayer = sender instanceof Player ? (Player) sender : null;

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

		if (sender != null && sender.hasPermission("easterlyn.command.selector") && id.length() > 1 && id.charAt(0) == '@') {
			// Theoretically selection accepts a UUID or player name, but why chance it on changes?
			for (Entity entity : Bukkit.selectEntities(sender, id)) {
				if (entity instanceof Player && (senderPlayer == null || senderPlayer.canSee((Player) entity))) {
					return (Player) entity;
				}
			}
			return null;
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
					|| StringUtil.startsWithIgnoreCase(player.getDisplayName(), id)) {
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
	 * @throws IllegalAccessException if attempting to match offline players on the main thread
	 */
	@SuppressWarnings("deprecation")
	@Nullable
	public static Player matchPlayer(@Nullable CommandSender sender, @NotNull String id, boolean offline,
			@Nullable Plugin plugin) throws IllegalAccessException {
		// TODO: nick support
		// Disallow on main thread - if we resort to searching offline players, this may take several seconds.
		if (Bukkit.isPrimaryThread() && offline) {
			throw new IllegalAccessException("Offline matching must be done asynchronously!");
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

		Player senderPlayer = sender instanceof Player ? (Player) sender : null;
		if (sender != null && sender.hasPermission("easterlyn.command.selector") && id.length() > 1 && id.charAt(0) == '@') {
			for (Entity entity : Bukkit.selectEntities(sender, id)) {
				if (entity instanceof Player && (senderPlayer == null || senderPlayer.canSee((Player) entity))) {
					return (Player) entity;
				}
			}
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
			if (player.hasPlayedBefore()) {
				return getPlayer(plugin, player.getUniqueId());
			}
		}
		// TODO faster inexact offline from usercache

		// Inexact online match
		player = Bukkit.getPlayer(id);

		if (player != null) {
			return player.getPlayer();
		}

		if (!offline) {
			return null;
		}

		id = id.toLowerCase();

		float bestMatch = 0F;
		for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
			String offlineName = offlinePlayer.getName();
			if (offlineName == null) {
				// Loaded by UUID only, name has never been looked up.
				continue;
			}

			float currentMatch = StringUtil.compare(id, offlinePlayer.getName().toLowerCase());

			if (currentMatch == 1F) {
				player = offlinePlayer;
				break;
			}

			if (currentMatch > bestMatch) {
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

	private PlayerUtil(){}

}
