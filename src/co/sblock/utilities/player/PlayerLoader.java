package co.sblock.utilities.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import org.bukkit.craftbukkit.v1_8_R2.CraftServer;

import net.minecraft.server.v1_8_R2.EntityPlayer;
import net.minecraft.server.v1_8_R2.MinecraftServer;
import net.minecraft.server.v1_8_R2.PlayerInteractManager;

/**
 * Utility for accessing offline Players as if they were online.
 * 
 * @author Jikoo
 */
public class PlayerLoader {

	public static Player getPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			// Online, life is easy.
			return player;
		}
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		String name = offlinePlayer.getName();
		if (name == null) {
			// Player has not logged in.
			return null;
		}
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		EntityPlayer nmsPlayer = new EntityPlayer(server, server.getWorldServer(0),
				new GameProfile(uuid, name), new PlayerInteractManager(server.getWorldServer(0)));

		player = (nmsPlayer == null) ? null : nmsPlayer.getBukkitEntity();
		if (player != null && player.hasPlayedBefore()) {
			player.loadData();
		}
		return player;
	}
}
