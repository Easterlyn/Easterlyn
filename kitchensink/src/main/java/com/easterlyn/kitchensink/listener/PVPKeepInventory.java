package com.easterlyn.kitchensink.listener;

import com.easterlyn.EasterlynCore;
import com.easterlyn.user.User;
import com.github.jikoo.planarwrappers.util.Experience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class PVPKeepInventory implements Listener {

  private final String key = "lastPVPDamage";

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    boolean playerDamage = false;
    if (event.getDamager() instanceof Player) {
      playerDamage = true;
    } else if (event.getDamager() instanceof Projectile projectile) {
      playerDamage = projectile.getShooter() instanceof Player;
    }

    if (!playerDamage) {
      return;
    }

    RegisteredServiceProvider<EasterlynCore> easterlynProvider =
        Bukkit.getServer().getServicesManager().getRegistration(EasterlynCore.class);
    if (easterlynProvider == null) {
      return;
    }

    User user =
        easterlynProvider.getProvider().getUserManager().getUser(event.getEntity().getUniqueId());
    user.getTemporaryStorage().put(key, System.currentTimeMillis());
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
    RegisteredServiceProvider<EasterlynCore> easterlynProvider =
        Bukkit.getServer().getServicesManager().getRegistration(EasterlynCore.class);
    if (easterlynProvider == null) {
      return;
    }

    User user =
        easterlynProvider.getProvider().getUserManager().getUser(event.getEntity().getUniqueId());
    Object object = user.getTemporaryStorage().get(key);
    if (!(object instanceof Long)) {
      return;
    }
    long timestamp = (long) object;
    if (timestamp < System.currentTimeMillis() - 6000L) {
      return;
    }
    event.setDroppedExp(Experience.getExp(event.getEntity()));
    int dropped = Experience.getExp(event.getEntity()) / 10;
    if (dropped > 30) {
      dropped = 30;
    }
    event.setDroppedExp(dropped);
    Experience.changeExp(event.getEntity(), -dropped);
    event.setKeepLevel(true);
    event.setKeepInventory(true);
    event.getDrops().clear();
  }
}
