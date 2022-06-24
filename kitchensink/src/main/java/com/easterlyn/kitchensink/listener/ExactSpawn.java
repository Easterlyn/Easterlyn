package com.easterlyn.kitchensink.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class ExactSpawn implements Listener {

  @EventHandler
  private void onSpawn(@NotNull PlayerSpawnLocationEvent event) {
    if (event.getPlayer().hasPlayedBefore()) {
      return;
    }

    World world = event.getSpawnLocation().getWorld();
    if (world == null) {
      world = Bukkit.getWorlds().get(0);
    }

    event.setSpawnLocation(world.getSpawnLocation());
  }

  @EventHandler
  private void onRespawn(@NotNull PlayerRespawnEvent event) {
    if (event.isAnchorSpawn() || event.isBedSpawn()) {
      return;
    }

    World world = event.getRespawnLocation().getWorld();
    if (world == null) {
      world = event.getPlayer().getWorld();
    }

    event.setRespawnLocation(world.getSpawnLocation());
  }

}
