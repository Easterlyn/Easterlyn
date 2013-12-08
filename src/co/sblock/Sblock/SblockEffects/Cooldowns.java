package co.sblock.Sblock.SblockEffects;

import java.util.Date;
import java.util.HashMap;

/**
 * A utility for tracking time remaining until a <code>Player</code> can use an ability/ActiveEffect again.
 * 
 * @author Jikoo
 *
 */
public class Cooldowns {
	/** A HashMap of all HashMaps of cooldowns remaining for each <code>Player</code> */
	private HashMap<String, HashMap<String, Long>> cooldowns = new HashMap<String, HashMap<String, Long>>();

	/** The Cooldowns instance */
	private static Cooldowns instance;

	/**
	 * Cooldowns singleton.
	 * 
	 * @return the current Cooldowns instance or a new instance if <code>null</code>
	 */
	public static Cooldowns getCooldowns() {
		if (instance == null) {
			instance = new Cooldowns();
		}
		return instance;
	}

	/**
	 * Gets a HashMap of all cooldowns for a <code>Player</code>, or, if they
	 * lacked an entry, creates a new one.
	 * 
	 * @param name
	 *            the name of the <code>Player</code>
	 * @return HashMap containing all cooldowns for a <code>Player</code>
	 */
	private HashMap<String, Long> getCooldownMap(String name) {
		if (cooldowns.containsKey(name)) {
			return cooldowns.get(name);
		} else {
			HashMap<String, Long> newMap = new HashMap<String, Long>();
			cooldowns.put(name, newMap);
			return newMap;
		}
	}

	/**
	 * Adds a cooldown for a <code>Player</code> of specified duration in milliseconds.
	 * 
	 * @param playerName
	 *            the name of the <code>Player</code>
	 * @param cooldownName
	 *            the name of the cooldown
	 * @param durationMillis
	 *            the length of the cooldown in milliseconds
	 */
	public void addCooldown(String playerName, String cooldownName, long durationMillis) {
		HashMap<String, Long> playerCooldowns = this.getCooldownMap(playerName);
		playerCooldowns.put(cooldownName, new Date().getTime() + durationMillis);
		cooldowns.put(playerName, playerCooldowns);
	}

	/**
	 * Removes a cooldown for the specified <code>Player</code>. If the
	 * <code>Player</code> has no remaining cooldowns on record, their entry is
	 * entirely removed.
	 * 
	 * @param playerName
	 *            the name of the <code>Player</code>
	 * @param cooldownName
	 *            the name of the cooldown
	 */
	public void clearCooldown(String playerName, String cooldownName) {
		if (!cooldowns.containsKey(playerName)) {
			return;
		}
		this.clearCooldownHelper(playerName, cooldownName);
	}

	/**
	 * Helper method for clearing cooldowns to reduce checks done by cleanup
	 * methods.
	 * 
	 * @param playerName
	 *            the name of the <code>Player</code>
	 * @param cooldownName
	 *            the name of the cooldown
	 */
	private void clearCooldownHelper(String playerName, String cooldownName) {
		HashMap<String, Long> playerCooldowns = this.getCooldownMap(playerName);
		playerCooldowns.remove(cooldownName);
		if (playerCooldowns.isEmpty()) {
			cooldowns.remove(playerName);
		} else {
			cooldowns.put(playerName, playerCooldowns);
		}
	}

	/**
	 * Fetches the time in milliseconds remaining for a <code>Player</code> on a
	 * certain cooldown.
	 * 
	 * @param name
	 *            the name of the Player
	 * @param cooldownName
	 *            the name of the cooldown
	 * @return the remaining milliseconds until specified ability can be re-used
	 */
	public long getRemainingMilliseconds(String name, String cooldownName) {
		if (!cooldowns.containsKey(cooldownName)) {
			return 0;
		}
		long now = new Date().getTime();
		if (cooldowns.get(name).get(cooldownName) - now > 0) {
			return cooldowns.get(name).get(cooldownName) - now;
		} else {
			return cooldowns.remove(name).get(cooldownName) - now;
		}
	}

	/**
	 * Removes all expired cooldown entries.
	 */
	public static void cleanup() {
		if (instance == null) {
			return;
		}
		long now = new Date().getTime();
		for (String player : instance.cooldowns.keySet()) {
			for (String cooldown : instance.cooldowns.get(player).keySet()) {
				if (instance.cooldowns.get(player).get(cooldown) <= now) {
					instance.clearCooldown(player, cooldown);
				}
			}
		}
	}

	/**
	 * Removes all expired cooldown entries for a specific <code>Player</code>.
	 */
	public static void cleanup(String player) {
		if (instance == null) {
			return;
		}
		if (!instance.cooldowns.containsKey(player)) {
			return;
		}
		long now = new Date().getTime();
		for (String cooldown : instance.cooldowns.get(player).keySet()) {
			if (instance.cooldowns.get(player).get(cooldown) <= now) {
				instance.clearCooldownHelper(player, cooldown);
			}
		}
	}
}
