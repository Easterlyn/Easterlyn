package com.easterlyn.kitchensink.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathCoordinates implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(PlayerDeathEvent event) {
    // TODO fun messages, clickable location component
    // TODO permanent record - maybe merge into /death
    Player player = event.getEntity();
    Location location = player.getLocation();
    String message =
        "Death point: {X} {Y} {Z}"
            .replace("{X}", String.valueOf(location.getBlockX()))
            .replace("{Y}", String.valueOf(location.getBlockY()))
            .replace("{Z}", String.valueOf(location.getBlockZ()));
    player.sendMessage("Oh dear, you are dead. " + message);
    Bukkit.getConsoleSender().sendMessage(player.getName() + " died. " + message);
  }
}
