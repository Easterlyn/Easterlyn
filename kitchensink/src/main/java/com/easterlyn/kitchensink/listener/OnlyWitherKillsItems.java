package com.easterlyn.kitchensink.listener;

import org.bukkit.entity.Item;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class OnlyWitherKillsItems implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Item) {
      event.setCancelled(
          !(event.getDamager() instanceof Wither) && !(event.getDamager() instanceof WitherSkull));
    }
  }
}
