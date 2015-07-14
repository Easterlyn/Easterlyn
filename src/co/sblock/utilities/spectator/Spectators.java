package co.sblock.utilities.spectator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import co.sblock.module.Module;
import co.sblock.utilities.general.Cooldowns;
import co.sblock.utilities.vote.SleepVote;

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

	/**
	 * @see co.sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		spectators = new HashMap<>();
	}

	/**
	 * @see co.sblock.Module#onDisable()
	 */
	@Override
	protected void onDisable() {
		instance = null;
		for (UUID u : spectators.keySet().toArray(new UUID[0])) {
			Player p = Bukkit.getPlayer(u);
			if (p != null) {
				this.removeSpectator(p);
			}
		}
	}

	/**
	 * Puts a player into spectator mode.
	 * 
	 * @param p the player to add
	 */
	public void addSpectator(Player p) {
		p.closeInventory();
		p.setGameMode(GameMode.SPECTATOR);
		SleepVote.getInstance().updateVoteCount(p.getWorld().getName(), p.getName());
		spectators.put(p.getUniqueId(), p.getLocation().add(0, .1, 0));
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

	public boolean canMineOre(Player p) {
		return Cooldowns.getInstance().getRemainder(p.getUniqueId(), "spectatore") <= 0;
	}

	/**
	 * Removes a player's spectator status.
	 * 
	 * @param p
	 */
	public void removeSpectator(Player p) {
		p.teleport(spectators.remove(p.getUniqueId()));
		p.setGameMode(GameMode.SURVIVAL);
		if (!p.hasPermission("sblock.command.spectate.nocooldown")) {
			// 8 minutes, 8 * 60 * 1000 ms
			Cooldowns.getInstance().addCooldown(p.getUniqueId(), "spectatore", 480000L);
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
