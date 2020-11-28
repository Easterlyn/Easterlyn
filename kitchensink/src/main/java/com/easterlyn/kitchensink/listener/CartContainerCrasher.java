package com.easterlyn.kitchensink.listener;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.inventory.ItemStack;

public class CartContainerCrasher implements Listener {

  @EventHandler(priority = EventPriority.LOW)
  public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
    if (event.getVehicle().getType() != EntityType.MINECART || !event.getVehicle().isValid()) {
      return;
    }

    if (event.getBlock().getType() == Material.DISPENSER
        || event.getBlock().getType() == Material.DROPPER) {
      BlockState blockState = event.getBlock().getState();
      if (!(blockState instanceof Container)) {
        return;
      }
      Container container = (Container) blockState;
      if (container.getInventory().firstEmpty() == -1) {
        return;
      }
      container.getInventory().addItem(new ItemStack(Material.MINECART));
      event.getVehicle().eject();
      event.getVehicle().remove();
    }
  }
}
