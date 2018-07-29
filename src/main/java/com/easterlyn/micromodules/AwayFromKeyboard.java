package com.easterlyn.micromodules;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.module.Module;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module for managing players who are inactive.
 *
 * @author Jikoo
 */
public class AwayFromKeyboard extends Module {

	private final Map<UUID, Location> lastLocations;
	private final Set<UUID> afkUUIDs;

	private Cooldowns cooldowns;
	private Language lang;

	public AwayFromKeyboard(Easterlyn plugin) {
		super(plugin);
		this.lastLocations = new HashMap<>();
		this.afkUUIDs = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

		PermissionUtils.addParent("easterlyn.afk.auto", UserRank.MEMBER.getPermission());
	}

	@Override
	protected void onEnable() {
		this.cooldowns = this.getPlugin().getModule(Cooldowns.class);
		this.lang = this.getPlugin().getModule(Language.class);

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					checkInactive(player);
					lastLocations.put(player.getUniqueId(), player.getLocation());
					if (player.hasPermission("easterlyn.afk.auto")
							&& cooldowns.getRemainder(player, getName()) == 0) {
						setInactive(player);
					}
				}
			}
		}.runTaskTimer(getPlugin(), 20L, 20L);
	}

	private void checkInactive(Player player) {
		Location last = this.lastLocations.get(player.getUniqueId());
		if (last == null) {
			// New player, not AFK
			this.setActive(player);
			return;
		}

		Location current = player.getLocation();

		if (!current.getWorld().equals(last.getWorld())) {
			this.setActive(player);
			return;
		}

		if (last.getPitch() == current.getPitch() && last.getYaw() == current.getYaw()) {
			// Players with the same exact pitch and yaw may be in a minecart, holding a button,
			// etc. They are not active.
			return;
		}

		double dX = Math.abs(last.getX() - current.getBlockX());
		double dY = Math.abs(last.getY() - current.getBlockY());
		double dZ = Math.abs(last.getZ() - current.getBlockZ());

		if (dX < 1 && dY < 1 && dZ < 1) {
			// Very short move, becoming afk
			return;
		}

		if (dX <= 4 && dY <= 4 && dZ <= 4) {
			// Short move, don't go afk if not afk
			this.extendActivity(player);
			return;
		}

		this.setActive(player);
	}

	/**
	 * If a Player is not AFK, extend their time before becoming AFK.
	 *
	 * @param player the Player
	 *
	 * @return true if time has been extended
	 */
	public boolean extendActivity(Player player) {
		if (this.cooldowns.getRemainder(player, getName()) > 0) {
			this.setActive(player);
			return true;
		}
		return false;
	}

	/**
	 * Mark a Player as not AFK or extend their time before becoming AFK.
	 *
	 * @param player the Player
	 */
	public void setActive(Player player) {
		if (this.afkUUIDs.contains(player.getUniqueId())) {
			Users.team(player, null);
			this.afkUUIDs.remove(player.getUniqueId());
			player.getPlayer().sendMessage(this.lang.getValue("afk.back"));
		}
		player.setSleepingIgnored(false);
		cooldowns.addCooldown(player, getName(), 300000L);
	}

	/**
	 * Gets whether or not a Player is AFK.
	 *
	 * @param player the Player
	 *
	 * @return true if the player is not AFK.
	 */
	public boolean isActive(Player player) {
		return !this.afkUUIDs.contains(player.getUniqueId());
	}

	/**
	 * Mark a Player as AFK.
	 *
	 * @param player the Player
	 */
	public void setInactive(Player player) {
		if (this.cooldowns.getRemainder(player, getName()) == 0) {
			player.setSleepingIgnored(true);
		}
		if (this.afkUUIDs.contains(player.getUniqueId())) {
			return;
		}
		this.afkUUIDs.add(player.getUniqueId());
		player.sendMessage(this.lang.getValue("afk.away"));
		Users.team(player, this.lang.getValue("afk.prefix"));
	}

	/**
	 * Clear a Player's AFK status and tracking.
	 *
	 * @param player the Player
	 */
	public void clearActivity(Player player) {
		UUID uuid = player.getUniqueId();
		if (this.afkUUIDs.contains(uuid)) {
			this.afkUUIDs.remove(uuid);
			Users.team(player, null);
		}
		if (this.lastLocations.containsKey(uuid)) {
			this.lastLocations.remove(uuid);
		}
		this.cooldowns.clearCooldown(player, getName());
	}

	@Override
	protected void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Users.team(player, null);
		}
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "AFK";
	}

}
