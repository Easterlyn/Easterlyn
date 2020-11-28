package com.easterlyn.netherportals.listener;

import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynNetherPortals;
import com.easterlyn.util.ProtectionUtil;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class TeleportListener implements Listener {

  private final EasterlynNetherPortals portals;

  public TeleportListener(EasterlynNetherPortals portals) {
    this.portals = portals;
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerPortal(PlayerPortalEvent event) {
    if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
        || event.getFrom().getWorld() == null) {
      return;
    }

    // Hijack portal creation entirely
    event.setCreationRadius(0);
    event.setCanCreatePortal(false);

    int radius = event.getFrom().getWorld().getEnvironment() == World.Environment.NETHER ? 8 : 1;
    event.setSearchRadius(radius);

    Location to = portals.getPortalDestination(event.getPlayer(), event.getFrom());

    if (to == null) {
      event.getPlayer().setPortalCooldown(40);
      event.setCancelled(true);
      return;
    }

    event.setTo(to);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityPortal(EntityPortalEvent event) {
    Block adjacentPortal = portals.getAdjacentPortal(event.getEntity(), event.getFrom().getBlock());
    if (adjacentPortal == null) {
      // Not a nether portal
      return;
    }

    // Don't allow event to progress - don't risk vanilla portal generation
    event.setCancelled(true);

    event.getEntity().setPortalCooldown(120);
    Location to = portals.getPortalDestination(event.getEntity(), adjacentPortal.getLocation());
    if (to != null) {
      event.getEntity().teleport(to, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
    }
  }

  @EventHandler
  public void onEntityPortalEnter(EntityPortalEnterEvent event) {
    if (event.getEntity() instanceof Player
        || event.getEntity().getPortalCooldown() > 0
        || event.getLocation().getBlock().getType() != Material.NETHER_PORTAL) {
      return;
    }

    List<Entity> passengers = event.getEntity().getPassengers();

    Player player = null;
    for (Entity passenger : passengers) {
      if (!passenger.getPassengers().isEmpty()) {
        return;
      }
      if (player == null && passenger instanceof Player) {
        // Match first rider - for boats, second player does not control direction.
        player = (Player) passenger;
      }
    }

    Location to =
        portals.getPortalDestination(
            player != null ? player : event.getEntity(), event.getLocation());

    if (to == null) {
      event.getEntity().setPortalCooldown(40);
      return;
    }

    event.getEntity().setPortalCooldown(40);
    event.getEntity().eject();

    if (!event.getEntity().teleport(to, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {
      return;
    }

    passengers.forEach(
        passenger -> {
          passenger.setPortalCooldown(40);
          passenger.teleport(to, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
          event.getEntity().addPassenger(passenger);
        });
  }

  @EventHandler
  public void onPortalCreate(PortalCreateEvent event) {
    if (event.getReason() != PortalCreateEvent.CreateReason.NETHER_PAIR) {
      return;
    }

    for (BlockState block : event.getBlocks()) {
      if (event.getEntity() instanceof Player) {
        if (!ProtectionUtil.canBuildAt((Player) event.getEntity(), block.getLocation())) {
          event.setCancelled(true);
          RegisteredServiceProvider<EasterlynCore> registration =
              portals.getServer().getServicesManager().getRegistration(EasterlynCore.class);
          if (registration != null) {
            registration
                .getProvider()
                .getLocaleManager()
                .sendMessage(event.getEntity(), "portals.denial");
          }
          break;
        }
      } else {
        if (ProtectionUtil.isProtected(block.getLocation())) {
          event.setCancelled(true);
          break;
        }
      }
    }

    if (event.isCancelled() && event.getEntity() != null) {
      // Force portal cooldown so entity doesn't repeatedly spam creation
      event.getEntity().setPortalCooldown(40);
    }
  }
}
