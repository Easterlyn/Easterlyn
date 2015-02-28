package co.sblock.utilities.spectator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import co.sblock.module.Module;

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

	private Map<UUID, Long> oreCooldown;

	/**
	 * @see co.sblock.Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		instance = this;
		spectators = new HashMap<>();
		oreCooldown = new HashMap<>();
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

	public Set<UUID> spectators() {
		return spectators.keySet();
	}

	/**
	 * Puts a player into spectator mode.
	 * 
	 * @param p the player to add
	 */
	public void addSpectator(Player p) {
		p.closeInventory();
		p.setGameMode(GameMode.SPECTATOR);
		spectators.put(p.getUniqueId(), p.getLocation());
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
		if (oreCooldown.containsKey(p.getUniqueId())) {
			return System.currentTimeMillis() > oreCooldown.get(p.getUniqueId());
		}
		return true;
	}

	/**
	 * Removes a player's spectator status.
	 * 
	 * @param p
	 */
	public void removeSpectator(Player p) {
		p.teleport(spectators.remove(p.getUniqueId()));
		p.setGameMode(GameMode.SURVIVAL);
		// 8 minutes, 8 * 60 * 1000 ms
		oreCooldown.put(p.getUniqueId(), System.currentTimeMillis() + 480000);
	}

	@Override
	protected String getModuleName() {
		return "Spectators";
	}

	/**
	 * Gets the Spectators instance.
	 * 
	 * @return the Spectators instance.
	 */
	public static Spectators getInstance() {
		return instance;
	}
}
