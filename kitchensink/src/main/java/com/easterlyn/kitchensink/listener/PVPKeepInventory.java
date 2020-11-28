package com.easterlyn.kitchensink.listener;

import com.easterlyn.EasterlynCore;
import com.easterlyn.user.User;
import com.easterlyn.util.ExperienceUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PVPKeepInventory implements Listener {

  private final String key = "lastPVPDamage";

  @EventHandler(ignoreCancelled = true)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    boolean playerDamage = false;
    if (event.getDamager() instanceof Player) {
      playerDamage = true;
    } else if (event.getDamager() instanceof Firework) {
      Firework firework = (Firework) event.getDamager();
      playerDamage =
          firework.getSpawningEntity() != null || firework.getShooter() instanceof Player;
    } else if (event.getDamager() instanceof Projectile) {
      playerDamage = ((Projectile) event.getDamager()).getShooter() instanceof Player;
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
  public void onPlayerDeath(PlayerDeathEvent event) {
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
    event.setDroppedExp(ExperienceUtil.getExp(event.getEntity()));
    int dropped = ExperienceUtil.getExp(event.getEntity()) / 10;
    if (dropped > 30) {
      dropped = 30;
    }
    event.setDroppedExp(dropped);
    ExperienceUtil.changeExp(event.getEntity(), -dropped);
    event.setKeepLevel(true);
    event.setKeepInventory(true);
    event.getDrops().clear();
  }
}
