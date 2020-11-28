package com.easterlyn.kitchensink.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class DeathDropProtection implements Listener {

  private final NamespacedKey key;

  public DeathDropProtection(Plugin plugin) {
    key = new NamespacedKey(plugin, "deathDrop");
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (event.getPlayer().isDead() || event.getPlayer().getHealth() <= 0) {
      event.getItemDrop().getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Item)
        || event.getCause() == EntityDamageEvent.DamageCause.VOID
        || event instanceof EntityDamageByEntityEvent) {
      // Ignore entity damage, OnlyWitherKillsItems takes care of that
      return;
    }
    if (event.getEntity().getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
      event.setCancelled(true);
    }
  }
}
