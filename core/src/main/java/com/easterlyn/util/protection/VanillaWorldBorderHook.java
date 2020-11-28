package com.easterlyn.util.protection;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

/**
 * ProtectionHook for vanilla's world border.
 *
 * @author Jikoo
 */
public class VanillaWorldBorderHook extends ProtectionHook {

  public VanillaWorldBorderHook() {
    super("minecraft");
  }

  public static boolean isOutsideBorder(Location location) {
    WorldBorder border = Objects.requireNonNull(location.getWorld()).getWorldBorder();
    double borderRadius = border.getSize() / 2;

    return border.getCenter().getX() - borderRadius > location.getBlockX()
        || border.getCenter().getX() + borderRadius < location.getBlockX()
        || border.getCenter().getZ() - borderRadius > location.getBlockZ()
        || border.getCenter().getZ() + borderRadius < location.getBlockZ();
  }

  @Override
  public boolean isHookUsable() {
    return true;
  }

  @Override
  public boolean isProtected(Location location) {
    return isOutsideBorder(location);
  }

  @Override
  public boolean canMobsSpawn(Location location) {
    return !this.isProtected(location);
  }

  @Override
  public boolean canUseButtonsAt(Player player, Location location) {
    return !this.isProtected(location);
  }

  @Override
  public boolean canOpenChestsAt(Player player, Location location) {
    return !this.isProtected(location);
  }

  @Override
  public boolean canBuildAt(Player player, Location location) {
    return !this.isProtected(location);
  }
}
