package com.easterlyn.micromodules.protectionhooks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

/**
 * Hook for the protection plugin <a href=http://dev.bukkit.org/bukkit-plugins/grief-prevention/>GriefPrevention</a>.
 *
 * @author Jikoo
 */
public class GriefPreventionHook extends ProtectionHook {

	public GriefPreventionHook() {
		super("GriefPrevention");
	}

	@Override
	public boolean isProtected(Location location) {
		return isHookUsable() && GriefPrevention.instance.dataStore.getClaimAt(location, true, null) != null;
	}

	@Override
	public boolean canMobsSpawn(Location location) {
		if (!isHookUsable()) {
			return true;
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
		// While mobs can spawn in admin claims, for the sake of ease,
		// our own protection hook will assume that it is disallowed.
		return claim == null || !claim.isAdminClaim();
	}

	@Override
	public boolean canUseButtonsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
		return claim == null || claim.allowAccess(player) == null;
	}

	@Override
	public boolean canOpenChestsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
		return claim == null || claim.allowContainers(player) == null;
	}

	@Override
	public boolean canBuildAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
		return claim == null || claim.allowBuild(player, Material.DIAMOND_BLOCK) == null;
	}

}
