package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class FreeCarts extends BaseCommand implements Listener {

  private final NamespacedKey key;

  public FreeCarts(Plugin plugin) {
    key = new NamespacedKey(plugin, "freecart");
  }

  @CommandAlias("freecart|tempcart")
  @Description("{@@sink.module.freecart.description}")
  @CommandPermission("easterlyn.command.freecart")
  @Syntax("<player> <x> <y> <z> <vectorX> <vectorY> <vectorZ>")
  @CommandCompletion("@player")
  // TODO check if also requires @double @double @double @double @double @double
  public void spawnFreeCart(
      @Flags(CoreContexts.ONLINE) Player target,
      double x,
      double y,
      double z,
      double vectorX,
      double vectorY,
      double vectorZ) {
    target
        .getWorld()
        .spawn(
            new Location(target.getWorld(), x, y, z),
            RideableMinecart.class,
            minecart -> {
              minecart.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
              minecart.addPassenger(target);
              minecart.setVelocity(new Vector(vectorX, vectorY, vectorZ));
            });
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Entity vehicle = event.getPlayer().getVehicle();
    if (vehicle != null && vehicle.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
      vehicle.eject();
      vehicle.remove();
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onVehicleExit(VehicleExitEvent event) {
    if (event.getExited().getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
      event.getExited().eject();
      event.getExited().remove();
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onVehicleDestroy(VehicleDestroyEvent event) {
    if (event.getVehicle().getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
      if (event.getAttacker() != null) {
        event.setCancelled(true);
        return;
      }
      event.getVehicle().eject();
      event.getVehicle().remove();
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
    if (event.getVehicle().getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
      if (event.getVehicle().getPassengers().size() > 0) {
        event.setCancelled(true);
        return;
      }
      event.getVehicle().eject();
      event.getVehicle().remove();
    }
  }

  @EventHandler
  public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
    if (event.getVehicle().getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
      event.getVehicle().eject();
      event.getVehicle().remove();
    }
  }
}
