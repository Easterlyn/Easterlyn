package co.sblock.micromodules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.module.Module;

/**
 * Module for managing players in spectator mode. Designed to allow players
 * to explore without giving any gamebreaking advantages.
 * 
 * @author Jikoo
 */
public class Spectators extends Module {

	/* The List of Players in spectator mode */
	private final Map<UUID, Location> spectators;

	/* The Cooldowns instance used to manage whether someone has recently been spectating. */
	private Cooldowns cooldowns;
	/* The SleepVote instance used to discount spectators from sleeping. */
	private SleepVote sleep;

	public Spectators(Sblock plugin) {
		super(plugin);
		spectators = new HashMap<>();
	}

	@Override
	protected void onEnable() {
		cooldowns = getPlugin().getModule(Cooldowns.class);
		sleep = getPlugin().getModule(SleepVote.class);

		new BukkitRunnable() {
			@Override
			public void run() {
				nextSpectator: for (Player player : Bukkit.getOnlinePlayers()) {
					if (!isSpectator(player.getUniqueId())) {
						continue;
					}
					if (player.hasPermission("sblock.command.spectate.unrestricted")) {
						continue;
					}
					if (cooldowns.getRemainder(player, getName()) == 0) {
						removeSpectator(player);
						player.sendMessage(Color.GOOD + "As your link to the astral plane fades, you awaken with a jolt.");
					}
					// 100 blocks from starting location
					Location start = spectators.get(player.getUniqueId());
					if (start.getWorld().equals(player.getWorld())
							&& start.distanceSquared(player.getLocation()) < 10000) {
						continue;
					}
					// 100 blocks from any player
					for (Player nearby : player.getWorld().getPlayers()) {
						if (nearby.equals(player)) {
							continue;
						}
						Location playerLoc;
						if (isSpectator(nearby.getUniqueId())) {
							playerLoc = spectators.get(nearby.getUniqueId());
						} else {
							playerLoc = nearby.getLocation();
						}
						if (playerLoc.getWorld().equals(player.getWorld())
								&& player.getLocation().distanceSquared(playerLoc) < 10000) {
							continue nextSpectator;
						}
					}
					removeSpectator(player);
					player.sendMessage(Color.GOOD + "With no one around to maintain your connection to the astral plane, you snap back to reality.");
				}
			}
		}.runTaskTimer(getPlugin(), 100, 100);
	}

	@Override
	protected void onDisable() {
		for (UUID uuid : spectators.keySet().toArray(new UUID[0])) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				this.removeSpectator(player);
			}
		}
	}

	/**
	 * Puts a player into spectator mode.
	 * 
	 * @param player the player to add
	 */
	public void addSpectator(Player player) {
		if (!this.isEnabled()) {
			return;
		}
		player.closeInventory();
		player.setGameMode(GameMode.SPECTATOR);
		if (sleep.updateVoteCount(player.getWorld().getName(), player.getName())) {
			player.sendMessage(Color.BAD + "You're no longer sleeping. You'll have to use a bed again to count towards the total.");
		}
		spectators.put(player.getUniqueId(), player.getLocation().add(0, .1, 0));
		if (!player.hasPermission("sblock.command.spectate.unrestricted")) {
			// Allow spectating for 30 minutes at a time
			cooldowns.addCooldown(player, getName(), 1800000);
		}
	}

	/**
	 * Check to see if a player is a spectator.
	 * 
	 * @param name the name of the player
	 * 
	 * @return true if the player is a spectator
	 */
	public boolean isSpectator(UUID userID) {
		return spectators.containsKey(userID);
	}

	public boolean canMineOre(Player player) {
		return cooldowns.getRemainder(player, "spectatore") <= 0;
	}

	/**
	 * Removes a player's spectator status.
	 * 
	 * @param player
	 */
	public void removeSpectator(Player player) {
		if (!this.isEnabled() || !this.isSpectator(player.getUniqueId())) {
			return;
		}
		cooldowns.clearCooldown(player, getName());
		player.teleport(spectators.remove(player.getUniqueId()));
		player.setGameMode(GameMode.SURVIVAL);
		if (!player.hasPermission("sblock.command.spectate.unrestricted")) {
			// 8 minutes, 8 * 60 * 1000 ms
			cooldowns.addCooldown(player, "spectatore", 480000L);
		}
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "Spectators";
	}
}
