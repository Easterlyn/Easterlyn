package com.easterlyn.micromodules.protectionhooks;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Hook for the protection plugin <a href=http://dev.bukkit.org/bukkit-plugins/worldguard/>WorldGuard</a>.
 *
 * @author Jikoo
 */
public class WorldGuardHook extends ProtectionHook {

	public WorldGuardHook() {
		super("WorldGuard");
	}

	@Override
	public boolean isProtected(Location location) {
		if (!isHookUsable()) {
			return false;
		}
		RegionManager regionManager = WGBukkit.getRegionManager(location.getWorld());
		if (regionManager == null) {
			return true;
		}
		return regionManager.getApplicableRegions(new Vector(location.getX(), location.getY(), location.getZ())).size() > 0;
	}

	@Override
	public boolean canMobsSpawn(Location location) {
		if (!isHookUsable()) {
			return true;
		}
		RegionManager regionManager = WGBukkit.getRegionManager(location.getWorld());
		if (regionManager == null) {
			return true;
		}
		ApplicableRegionSet regions = regionManager.getApplicableRegions(new Vector(location.getX(), location.getY(), location.getZ()));
		return regions.queryState(null, DefaultFlag.MOB_SPAWNING) == State.ALLOW;
	}

	@Override
	public boolean canUseButtonsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		RegionManager regionManager = WGBukkit.getRegionManager(location.getWorld());
		if (regionManager == null) {
			return true;
		}
		ApplicableRegionSet regions = regionManager.getApplicableRegions(new Vector(location.getX(), location.getY(), location.getZ()));
		return regions.queryState(null, DefaultFlag.ENTRY, DefaultFlag.USE) == State.ALLOW;
	}

	@Override
	public boolean canOpenChestsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		RegionManager regionManager = WGBukkit.getRegionManager(location.getWorld());
		if (regionManager == null) {
			return true;
		}
		ApplicableRegionSet regions = regionManager.getApplicableRegions(new Vector(location.getX(), location.getY(), location.getZ()));
		return regions.queryState(null, DefaultFlag.ENTRY, DefaultFlag.CHEST_ACCESS) == State.ALLOW;
	}

	@Override
	public boolean canBuildAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		RegionManager regionManager = WGBukkit.getRegionManager(location.getWorld());
		if (regionManager == null) {
			return true;
		}
		ApplicableRegionSet regions = regionManager.getApplicableRegions(new Vector(location.getX(), location.getY(), location.getZ()));
		return regions.queryState(null, DefaultFlag.ENTRY, DefaultFlag.BUILD) == State.ALLOW;
	}

}
