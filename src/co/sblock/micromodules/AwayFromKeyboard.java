package co.sblock.micromodules;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.module.Module;
import co.sblock.users.Users;

/**
 * Module for managing players who are inactive.
 * 
 * @author Jikoo
 */
public class AwayFromKeyboard extends Module {

	private final Map<UUID, Location> lastLocations;
	private final Set<UUID> afkUUIDs;

	private Cooldowns cooldowns;

	public AwayFromKeyboard(Sblock plugin) {
		super(plugin);
		this.lastLocations = new HashMap<>();
		this.afkUUIDs = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

		Permission permission = Bukkit.getPluginManager().getPermission("sblock.afk.auto");
		if (permission == null) {
			permission = new Permission("sblock.afk.auto");
			Bukkit.getPluginManager().addPermission(permission);
		}
		permission.addParent("sblock.default", true).recalculatePermissibles();
	}

	@Override
	protected void onEnable() {
		this.cooldowns = getPlugin().getModule(Cooldowns.class);

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					checkInactive(player);
					lastLocations.put(player.getUniqueId(), player.getLocation());
					if (player.hasPermission("sblock.afk.auto")
							&& cooldowns.getRemainder(player, getName()) == 0) {
						setInactive(player);
					}
				}
			}
		}.runTaskTimer(getPlugin(), 20L, 20L);
	}

	private void checkInactive(Player player) {
		Location last = lastLocations.get(player.getUniqueId());
		if (last == null) {
			// New player, not AFK
			setActive(player);
			return;
		}

		Location current = player.getLocation();

		if (!current.getWorld().equals(last.getWorld())) {
			setActive(player);
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
			extendActivity(player);
			return;
		}

		setActive(player);
	}

	/**
	 * If a Player is not AFK, extend their time before becoming AFK.
	 * 
	 * @param player the Player
	 * 
	 * @return true if time has been extended
	 */
	public boolean extendActivity(Player player) {
		if (cooldowns.getRemainder(player, getName()) > 0) {
			setActive(player);
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
		if (afkUUIDs.contains(player.getUniqueId())) {
			Users.team(player, null);
			afkUUIDs.remove(player.getUniqueId());
			player.sendMessage(Color.GOOD + "You are no longer marked as away!");
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
		return !afkUUIDs.contains(player.getUniqueId());
	}

	/**
	 * Mark a Player as AFK.
	 * 
	 * @param player the Player
	 */
	public void setInactive(Player player) {
		if (cooldowns.getRemainder(player, getName()) == 0) {
			player.setSleepingIgnored(true);
		}
		if (afkUUIDs.contains(player.getUniqueId())) {
			return;
		}
		afkUUIDs.add(player.getUniqueId());
		player.sendMessage(Color.GOOD + "You have been marked as away!");
		Users.team(player, Color.GOOD_EMPHASIS + "[AFK] ");
	}

	/**
	 * Clear a Player's AFK status and tracking.
	 * 
	 * @param player the Player
	 */
	public void clearActivity(Player player) {
		UUID uuid = player.getUniqueId();
		if (afkUUIDs.contains(uuid)) {
			afkUUIDs.remove(uuid);
			Users.team(player, null);
		}
		if (lastLocations.containsKey(uuid)) {
			lastLocations.remove(uuid);
		}
		cooldowns.clearCooldown(player, getName());
	}

	@Override
	protected void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Users.team(player, null);
		}
	}

	@Override
	public String getName() {
		return "AFK";
	}

}
