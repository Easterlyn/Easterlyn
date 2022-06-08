package com.easterlyn.util.wrapper;

import com.easterlyn.EasterlynCore;
import com.easterlyn.util.PermissionUtil;
import java.util.concurrent.Phaser;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

/**
 * A CraftPlayer implementation that directly uses our bridge for handling permissions.
 *
 * @author Jikoo
 */
public class PermissiblePlayer extends CraftPlayer {

  private final Phaser phaser;

  public PermissiblePlayer(CraftPlayer player) {
    super((CraftServer) player.getServer(), player.getHandle());
    this.phaser = new Phaser(1);
    if (Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              EasterlynCore.getPlugin(EasterlynCore.class),
              () -> {
                PermissionUtil.loadPermissionData(player.getUniqueId());
                phaser.arriveAndDeregister();
              });
    } else {
      PermissionUtil.loadPermissionData(player.getUniqueId());
      phaser.arriveAndDeregister();
    }
  }

  @Override
  public boolean hasPermission(@NotNull String arg0) {
    phaser.awaitAdvance(phaser.getPhase() + 1);
    return PermissionUtil.hasPermission(getUniqueId(), arg0);
  }

  @Override
  public boolean hasPermission(@NotNull Permission arg0) {
    return this.hasPermission(arg0.getName());
  }

  @Override
  public boolean isPermissionSet(@NotNull String arg0) {
    return true;
  }

  @Override
  public boolean isPermissionSet(@NotNull Permission arg0) {
    return true;
  }
}
