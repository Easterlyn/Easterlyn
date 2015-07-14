package co.sblock.utilities.general;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility for tracking time remaining until a Player can use an ability/ActiveEffect again.
 * 
 * @author Jikoo
 */
public class Cooldowns {

	private static Cooldowns instance;

	private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> cooldowns = new ConcurrentHashMap<>();
	private final UUID global = UUID.fromString("00000000-0000-0000-0000-000000000000");

	/**
	 * Cooldowns singleton.
	 * 
	 * @return the current Cooldowns instance or a new instance if null
	 */
	public static Cooldowns getInstance() {
		if (instance == null) {
			instance = new Cooldowns();
		}
		return instance;
	}

	/**
	 * Gets a HashMap of all cooldowns for a Player, or, if they lacked an entry, creates a new one.
	 * 
	 * @param uuid the UUID of the Player
	 * @return HashMap containing all cooldowns
	 */
	private ConcurrentHashMap<String, Long> getCooldownMap(UUID uuid) {
		if (cooldowns.containsKey(uuid)) {
			return cooldowns.get(uuid);
		} else {
			ConcurrentHashMap<String, Long> newMap = new ConcurrentHashMap<>();
			cooldowns.put(uuid, newMap);
			return newMap;
		}
	}

	/**
	 * Adds a cooldown for a Player of specified duration in milliseconds.
	 * 
	 * @param uuid the UUID of the Player
	 * @param cooldownName the name of the cooldown
	 * @param durationMillis the length of the cooldown in milliseconds
	 */
	public void addCooldown(UUID uuid, String cooldownName, long durationMillis) {
		getCooldownMap(uuid).put(cooldownName, System.currentTimeMillis() + durationMillis);
	}

	/**
	 * Adds a global cooldown of specified duration in milliseconds.
	 * 
	 * @param cooldownName the name of the cooldown
	 * @param durationMillis the length of the cooldown in milliseconds
	 */
	public void addGlobalCooldown(String cooldownName, long durationMillis) {
		getCooldownMap(global).put(cooldownName, System.currentTimeMillis() + durationMillis);
	}

	/**
	 * Removes a cooldown for the specified Player. If the Player has no remaining cooldowns on
	 * record, their entry is entirely removed.
	 * 
	 * @param uuid the UUID of the Player
	 * @param cooldownName the name of the cooldown
	 */
	public void clearCooldown(UUID uuid, String cooldownName) {
		if (!cooldowns.containsKey(uuid)) {
			return;
		}
		ConcurrentHashMap<String, Long> playerCooldowns = this.getCooldownMap(uuid);
		playerCooldowns.remove(cooldownName);
		if (playerCooldowns.isEmpty()) {
			cooldowns.remove(uuid);
		}
	}

	/**
	 * Fetches the time in milliseconds remaining for a Player on a certain cooldown.
	 * 
	 * @param uuid the UUID of the Player
	 * @param cooldownName the name of the cooldown
	 * @return the remaining milliseconds until specified ability can be re-used
	 */
	public long getRemainder(UUID uuid, String cooldownName) {
		if (!cooldowns.containsKey(uuid) || !cooldowns.get(uuid).containsKey(cooldownName)) {
			return 0;
		}
		long remaining = cooldowns.get(uuid).get(cooldownName) - System.currentTimeMillis();
		if (remaining > 0) {
			return remaining;
		} else {
			clearCooldown(uuid, cooldownName);
			return 0;
		}
	}

	/**
	 * Fetches the time in milliseconds remaining for a global cooldown.
	 * 
	 * @param uuid the UUID of the Player
	 * @param cooldownName the name of the cooldown
	 * @return the remaining milliseconds until specified ability can be re-used
	 */
	public long getGlobalRemainder(String cooldownName) {
		if (!cooldowns.containsKey(global) || !cooldowns.get(global).containsKey(cooldownName)) {
			return 0;
		}
		long remaining = cooldowns.get(global).get(cooldownName) - System.currentTimeMillis();
		if (remaining > 0) {
			return remaining;
		} else {
			clearCooldown(global, cooldownName);
			return 0;
		}
	}

	/**
	 * Removes all expired cooldown entries.
	 */
	public static void cleanup() {
		if (instance == null) {
			return;
		}
		long now = System.currentTimeMillis();
		for (UUID uuid : instance.cooldowns.keySet()) {
			for (String cooldown : instance.cooldowns.get(uuid).keySet()) {
				if (instance.cooldowns.get(uuid).get(cooldown) <= now) {
					instance.clearCooldown(uuid, cooldown);
				}
			}
		}
	}

	/**
	 * Removes all expired cooldown entries for a specific <code>Player</code>.
	 */
	public static void cleanup(UUID uuid) {
		if (instance == null) {
			return;
		}
		if (!instance.cooldowns.containsKey(uuid)) {
			return;
		}
		long now = System.currentTimeMillis();
		for (String cooldown : instance.cooldowns.get(uuid).keySet()) {
			if (instance.cooldowns.get(uuid).get(cooldown) <= now) {
				ConcurrentHashMap<String, Long> playerCooldowns = instance.getCooldownMap(uuid);
				playerCooldowns.remove(cooldown);
				if (playerCooldowns.isEmpty()) {
					instance.cooldowns.remove(uuid);
				}
			}
		}
	}
}
