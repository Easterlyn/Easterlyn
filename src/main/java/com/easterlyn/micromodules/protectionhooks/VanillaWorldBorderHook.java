package com.easterlyn.micromodules.protectionhooks;

import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

/**
 * VanillaWorldBorderHook
 *
 * @author Jikoo
 */
public class VanillaWorldBorderHook extends ProtectionHook {

	public VanillaWorldBorderHook() {
		super("minecraft");
	}

	@Override
	public boolean isHookUsable() {
		return true;
	}

	@Override
	public boolean isProtected(Location location) {
		WorldBorder border = location.getWorld().getWorldBorder();
		double borderRadius = border.getSize() / 2;

		return border.getCenter().getX() - borderRadius > location.getBlockX()
				|| border.getCenter().getX() + borderRadius < location.getBlockX()
				|| border.getCenter().getZ() - borderRadius > location.getBlockZ()
				|| border.getCenter().getZ() + borderRadius < location.getBlockZ();
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
