package co.sblock.utilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import co.sblock.Sblock;

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
	 * Adds a cooldown for an Entity of specified duration in milliseconds.
	 * 
	 * @param entity the Entity
	 * @param cooldownName the name of the cooldown
	 * @param durationMillis the length of the cooldown in milliseconds
	 */
	public void addCooldown(Entity entity, String cooldownName, long durationMillis) {
		long expiry = System.currentTimeMillis() + durationMillis;
		if (entity instanceof Player) {
			getCooldownMap(entity.getUniqueId()).put(cooldownName, expiry);
			return;
		}

		entity.setMetadata(cooldownName, new FixedMetadataValue(Sblock.getInstance(), expiry));
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
	 * Removes a global cooldown. As global cooldowns are constantly changed, the global cooldown
	 * entry is not removed if empty.
	 * 
	 * @param cooldownName the name of the cooldown
	 */
	public void clearGlobalCooldown(String cooldownName) {
		getCooldownMap(global).remove(cooldownName);
	}

	/**
	 * Removes a cooldown for the specified Entity. If the Entity has no remaining cooldowns on
	 * record, their entry is entirely removed.
	 * 
	 * @param entity the Entity
	 * @param cooldownName the name of the cooldown
	 */
	public void clearCooldown(Entity entity, String cooldownName) {
		if (entity instanceof Player) {
			if (!cooldowns.containsKey(entity.getUniqueId())) {
				return;
			}
			ConcurrentHashMap<String, Long> playerCooldowns = this.getCooldownMap(entity.getUniqueId());
			playerCooldowns.remove(cooldownName);
			if (playerCooldowns.isEmpty()) {
				cooldowns.remove(entity.getUniqueId());
			}
			return;
		}

		entity.removeMetadata(cooldownName, Sblock.getInstance());
	}

	/**
	 * Fetches the time in milliseconds remaining for a Player on a certain cooldown.
	 * 
	 * @param uuid the UUID of the Player
	 * @param cooldownName the name of the cooldown
	 * @return the remaining milliseconds until specified ability can be re-used
	 */
	public long getRemainder(Entity entity, String cooldownName) {
		if (entity instanceof Player) {
			if (!cooldowns.containsKey(entity.getUniqueId()) || !cooldowns.get(entity.getUniqueId()).containsKey(cooldownName)) {
				return 0;
			}
			long remaining = cooldowns.get(entity.getUniqueId()).get(cooldownName) - System.currentTimeMillis();
			if (remaining > 0) {
				return remaining;
			} else {
				clearCooldown(entity, cooldownName);
				return 0;
			}
		}

		if (!entity.hasMetadata(cooldownName)) {
			return 0;
		}

		long remaining = 0;

		for (MetadataValue value : entity.getMetadata(cooldownName)) {
			if (value.getOwningPlugin().equals(Sblock.getInstance())) {
				remaining = value.asLong();
				break;
			}
		}

		if (remaining < 0) {
			return 0;
		}
		return remaining;
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
			ConcurrentHashMap<String, Long> globals = this.getCooldownMap(global);
			globals.remove(cooldownName);
			if (globals.isEmpty()) {
				cooldowns.remove(global);
			}
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
			Map<String, Long> map = instance.cooldowns.get(uuid);
			for (String cooldown : map.keySet()) {
				if (map.get(cooldown) <= now) {
					map.remove(cooldown);
					if (map.isEmpty()) {
						instance.cooldowns.remove(map);
					}
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
