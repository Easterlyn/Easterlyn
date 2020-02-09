package com.easterlyn.netherportals.listener;

import com.easterlyn.EasterlynNetherPortals;
import com.easterlyn.util.ProtectionUtil;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

	private final EasterlynNetherPortals plugin;

	public TeleportListener(EasterlynNetherPortals plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {
		// TODO update for new API
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
			return;
		}

		Location to = plugin.getPortalFrom(event.getFrom(), location -> ProtectionUtil.canBuildAt(event.getPlayer(), location));

		if (to == null) {
			event.getPlayer().sendMessage("You do not have access to spawn a nether portal here!");
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);
		event.getPlayer().teleport(to, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
	}

	@EventHandler
	public void onEntityPortalEnter(EntityPortalEnterEvent event) {
		if (event.getEntity() instanceof Player || event.getEntity().getPortalCooldown() > 0
				|| event.getLocation().getBlock().getType() != Material.NETHER_PORTAL) {
			return;
		}

		List<Entity> passengers = event.getEntity().getPassengers();

		Player passengerPlayer = null;
		for (Entity passenger : passengers) {
			if (!passenger.getPassengers().isEmpty()) {
				return;
			}
			if (passengerPlayer == null && passenger instanceof Player) {
				// Match first rider - for boats, second player does not control direction.
				passengerPlayer = (Player) passenger;
			}
		}

		Player player = passengerPlayer;

		Location to = plugin.getPortalFrom(event.getLocation(),
				player != null ? location -> ProtectionUtil.canBuildAt(player, location) : ProtectionUtil::isProtected);

		if (to == null) {
			event.getEntity().setPortalCooldown(40);
			if (player != null) {
				player.sendMessage("You do not have access to spawn a nether portal here!");
			}
			return;
		}

		event.getEntity().setPortalCooldown(40);
		event.getEntity().eject();

		if (!event.getEntity().teleport(to, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {
			return;
		}

		passengers.forEach(passenger -> {
			passenger.setPortalCooldown(40);
			passenger.teleport(to, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
			event.getEntity().addPassenger(passenger);
		});
	}

}
