package com.easterlyn.micromodules.protectionhooks;

import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

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
		RegionQuery query = WGBukkit.getPlugin().getRegionContainer().createQuery();
		return query.getApplicableRegions(location).size() > 0;
	}

	@Override
	public boolean canMobsSpawn(Location location) {
		if (!isHookUsable()) {
			return true;
		}
		RegionQuery query = WGBukkit.getPlugin().getRegionContainer().createQuery();
		return query.queryState(location, (RegionAssociable) null, DefaultFlag.MOB_SPAWNING) == State.ALLOW;
	}

	@Override
	public boolean canUseButtonsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		RegionQuery query = WGBukkit.getPlugin().getRegionContainer().createQuery();
		return query.queryState(location, player, DefaultFlag.ENTRY, DefaultFlag.USE) == State.ALLOW;
	}

	@Override
	public boolean canOpenChestsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		RegionQuery query = WGBukkit.getPlugin().getRegionContainer().createQuery();
		return query.queryState(location, player, DefaultFlag.ENTRY, DefaultFlag.CHEST_ACCESS) == State.ALLOW;
	}

	@Override
	public boolean canBuildAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		RegionQuery query = WGBukkit.getPlugin().getRegionContainer().createQuery();
		return query.queryState(location, player, DefaultFlag.ENTRY, DefaultFlag.BUILD) == State.ALLOW;
	}

}
