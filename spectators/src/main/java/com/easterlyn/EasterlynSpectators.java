package com.easterlyn;

import com.easterlyn.user.User;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.event.SimpleListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EasterlynSpectators extends JavaPlugin {

	public static final String USER_SPECTATE_RETURN = "spectate.return";
	public static final String USER_SPECTATE_COOLDOWN = "spectate.cooldown";
	public static final String USER_SPECTPA = "spectate.tpa";

	@Override
	public void onEnable() {
		PermissionUtil.addParent("easterlyn.spectators.unrestricted", UserRank.STAFF.getPermission());
		PermissionUtil.addParent("easterlyn.spectators.nightvision", UserRank.MODERATOR.getPermission());

		BlockBreakEvent.getHandlerList().register(new SimpleListener<>(BlockBreakEvent.class, event -> {
			if (event.getPlayer().hasPermission("easterlyn.spectators.unrestricted")) {
				return;
			}

			RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
			if (registration == null) {
				return;
			}

			User user = registration.getProvider().getUserManager().getUser(event.getPlayer().getUniqueId());
			long spectatore = user.getStorage().getLong(USER_SPECTATE_COOLDOWN, 0);

			if (spectatore < System.currentTimeMillis()) {
				return;
			}

			for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST,
					BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.SELF, BlockFace.DOWN }) {
				Block block = event.getBlock().getRelative(face);
				if (block.getType() == Material.NETHER_QUARTZ_ORE) {
					block.setType(Material.NETHERRACK);
				}
				if (block.getType().name().endsWith("_ORE")) {
					block.setType(Material.STONE);
				}
			}
		}, this, EventPriority.LOW));

		PlayerQuitEvent.getHandlerList().register(new SimpleListener<>(PlayerQuitEvent.class, event -> {
			RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
			if (registration == null) {
				return;
			}

			Player player = event.getPlayer();
			if (player.getGameMode() != GameMode.SPECTATOR) {
				return;
			}

			User user = registration.getProvider().getUserManager().getUser(player.getUniqueId());
			Location spectateReturn = user.getStorage().getSerializable(USER_SPECTATE_RETURN, Location.class);
			if (spectateReturn == null) {
				return;
			}

			player.teleport(spectateReturn);
		}, this));

		PlayerJoinEvent.getHandlerList().register(new SimpleListener<>(PlayerJoinEvent.class, event -> {
			RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
			if (registration == null) {
				return;
			}

			Player player = event.getPlayer();
			User user = registration.getProvider().getUserManager().getUser(player.getUniqueId());
			Location spectateReturn = user.getStorage().getSerializable(USER_SPECTATE_RETURN, Location.class);

			if (spectateReturn == null) {
				return;
			}

			user.getStorage().set(USER_SPECTATE_RETURN, null);
			user.getStorage().set(USER_SPECTATE_COOLDOWN, System.currentTimeMillis() + 480000L);
			player.teleport(spectateReturn);

			if (player.getGameMode() == GameMode.SPECTATOR) {
				player.setGameMode(GameMode.SURVIVAL);
			}
		}, this));

		PlayerTeleportEvent.getHandlerList().register(new SimpleListener<>(PlayerTeleportEvent.class, event -> {
			if (event.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE) {
				return;
			}

			RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
			if (registration == null) {
				return;
			}

			Player player = event.getPlayer();
			User user = registration.getProvider().getUserManager().getUser(player.getUniqueId());

			// Don't block request-based teleportation
			if (user.getStorage().getBoolean(USER_SPECTPA)) {
				return;
			}

			Location spectateReturn = user.getStorage().getSerializable(USER_SPECTATE_RETURN, Location.class);

			if (spectateReturn != null) {
				event.setCancelled(true);
				player.sendMessage("Spectating via hotbar is currently disabled. Please use `/spectpa`!");
			}
		}, this));

		PlayerGameModeChangeEvent.getHandlerList().register(new SimpleListener<>(PlayerGameModeChangeEvent.class, event -> {
			RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
			if (registration == null) {
				return;
			}

			Player player = event.getPlayer();
			if (player.getGameMode() != GameMode.SPECTATOR) {
				return;
			}

			User user = registration.getProvider().getUserManager().getUser(player.getUniqueId());
			Location spectateReturn = user.getStorage().getSerializable(USER_SPECTATE_RETURN, Location.class);
			if (spectateReturn != null) {
				event.setCancelled(true);
			}
		}, this));

		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class,
				pluginEnableEvent -> {
					if (pluginEnableEvent.getPlugin() instanceof EasterlynCore) {
						register((EasterlynCore) pluginEnableEvent.getPlugin());
					}
				}, this));
	}

	private void register(EasterlynCore plugin) {
		plugin.registerCommands(this, this.getClassLoader(), "com.easterlyn.spectators.command");
	}

	@Override
	public void onDisable() {
		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration == null) {
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				continue;
			}

			User user = registration.getProvider().getUserManager().getUser(player.getUniqueId());
			Location spectateReturn = user.getStorage().getSerializable(USER_SPECTATE_RETURN, Location.class);
			if (spectateReturn == null) {
				continue;
			}
			user.getStorage().set(USER_SPECTATE_RETURN, null);
			player.teleport(spectateReturn);
			player.setGameMode(GameMode.SURVIVAL);
			user.getStorage().set(USER_SPECTATE_COOLDOWN, System.currentTimeMillis() + 480000L);
		}

	}

}
