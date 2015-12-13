package co.sblock.micromodules;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.module.Module;
import co.sblock.users.Users;

/**
 * Module for managing players who are inactive.
 * 
 * @author Jikoo
 */
public class AwayFromKeyboard extends Module {

	private final HashMap<UUID, Location> lastLocations;

	private Cooldowns cooldowns;

	public AwayFromKeyboard(Sblock plugin) {
		super(plugin);
		this.lastLocations = new HashMap<>();
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
					// TODO get cooldown, if 0 set -> AFK
				}
			}
		}.runTaskTimer(getPlugin(), 100L, 100L);
	}

	private void checkInactive(Player player) {
		Location last = lastLocations.get(player.getUniqueId());
		if (last == null) {
			// New player, not AFK
			setActive(player, false);
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

		if (dX <= 5 && dY <= 5 && dZ <= 5) {
			extendActivity(player);
			return;
		}

		setActive(player);
	}

	/**
	 * If a player is not AFK, extend their time before becoming AFK.
	 * 
	 * @param player the Player
	 * 
	 * @return true if time has been extended
	 */
	public boolean extendActivity(Player player) {
		if (cooldowns.getRemainder(player, getName()) > 0) {
			setActive(player, false);
			return true;
		}
		return false;
	}

	/**
	 * Mark a player as not AFK or extend their time before becoming AFK.
	 * 
	 * @param player the Player
	 */
	public void setActive(Player player) {
		setActive(player, true);
	}

	private void setActive(Player player, boolean setTeam) {
		if (setTeam && cooldowns.getRemainder(player, getName()) == 0) {
			Users.team(player, null);
		}
		cooldowns.addCooldown(player, getName(), 300000L);
		// TODO set -> not AFK
	}

	public boolean isActive(Player player) {
		return cooldowns.getRemainder(player, getName()) > 0;
	}

	public void setInactive(Player player) {
		cooldowns.clearCooldown(player, getName());
		// TODO decision
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
