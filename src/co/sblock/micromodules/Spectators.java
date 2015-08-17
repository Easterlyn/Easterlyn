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
import co.sblock.utilities.Cooldowns;

/**
 * Module for managing players in spectator mode. Designed to allow players
 * to explore without giving any gamebreaking advantages.
 * 
 * @author Jikoo
 */
public class Spectators extends Module {

	/* The Spectators instance. */
	private static Spectators instance;

	/* The List of Players in spectator mode */
	private Map<UUID, Location> spectators;

	@Override
	protected void onEnable() {
		instance = this;
		spectators = new HashMap<>();

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (isSpectator(player.getUniqueId())
							&& Cooldowns.getInstance().getRemainder(player, getModuleName()) == 0) {
						removeSpectator(player);
						player.sendMessage(Color.GOOD + "As your link to the astral plane fades, you awaken with a jolt.");
					}
				}
			}
		}.runTaskTimer(Sblock.getInstance(), 600, 600);
	}

	@Override
	protected void onDisable() {
		instance = null;
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
		player.closeInventory();
		player.setGameMode(GameMode.SPECTATOR);
		SleepVote.getInstance().updateVoteCount(player.getWorld().getName(), player.getName());
		spectators.put(player.getUniqueId(), player.getLocation().add(0, .1, 0));
		// Allow spectating for 30 minutes at a time
		Cooldowns.getInstance().addCooldown(player, getModuleName(), 1800000);
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
		return Cooldowns.getInstance().getRemainder(player, "spectatore") <= 0;
	}

	/**
	 * Removes a player's spectator status.
	 * 
	 * @param player
	 */
	public void removeSpectator(Player player) {
		Cooldowns.getInstance().clearCooldown(player, getModuleName());
		player.teleport(spectators.remove(player.getUniqueId()));
		player.setGameMode(GameMode.SURVIVAL);
		if (!player.hasPermission("sblock.command.spectate.nocooldown")) {
			// 8 minutes, 8 * 60 * 1000 ms
			Cooldowns.getInstance().addCooldown(player, "spectatore", 480000L);
		}
	}

	/**
	 * Gets the Spectators instance.
	 * 
	 * @return the Spectators instance.
	 */
	public static Spectators getInstance() {
		return instance;
	}

	@Override
	protected String getModuleName() {
		return "Spectators";
	}
}
