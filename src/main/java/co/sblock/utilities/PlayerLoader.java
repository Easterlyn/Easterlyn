package co.sblock.utilities;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.mojang.authlib.GameProfile;

import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;

/**
 * Utility for accessing offline Players as if they were online.
 * 
 * @author Jikoo
 */
public class PlayerLoader {

	private static final Cache<UUID, Player> PLAYER_CACHE = CacheBuilder.newBuilder().weakValues()
			.expireAfterAccess(5, TimeUnit.MINUTES).build();

	public static Player getPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			// Online, life is easy.
			return player;
		}
		player = PLAYER_CACHE.getIfPresent(uuid);
		if (player != null) {
			return player;
		}
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		if (offlinePlayer == null || offlinePlayer.getName() == null) {
			// Player has not logged in.
			return null;
		}
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		EntityPlayer nmsPlayer = new EntityPlayer(server, server.getWorldServer(0),
				new GameProfile(uuid, offlinePlayer.getName()),
				new PlayerInteractManager(server.getWorldServer(0)));

		player = (nmsPlayer == null) ? null : nmsPlayer.getBukkitEntity();
		if (player == null) {
			return null;
		}
		if (player.hasPlayedBefore()) {
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
