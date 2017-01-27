package com.easterlyn.utilities;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PlayerInteractManager;

import org.bukkit.craftbukkit.v1_11_R1.CraftServer;

/**
 * Utility for accessing offline Players as if they were online.
 * 
 * @author Jikoo
 */
public class PlayerLoader {

	private static final Cache<UUID, Player> PLAYER_CACHE = CacheBuilder.newBuilder().weakValues()
			.expireAfterAccess(5, TimeUnit.MINUTES).maximumSize(50).build();

	public static Player getPlayer(Plugin plugin, UUID uuid) {
		return getPlayer(plugin, uuid, true);
	}

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
			throw new IllegalStateException("Asynchronous player load must use PlayerLoader#getPlayer(Plugin, UUID)");
		}
		Future<Player> future = Bukkit.getScheduler().callSyncMethod(plugin,
				new Callable<Player>() {
					@Override
					public Player call() throws Exception {
						return getPlayerFor(uuid);
					}
				});

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

		Player player = (nmsPlayer == null) ? null : nmsPlayer.getBukkitEntity();
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

}
