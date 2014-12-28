package co.sblock.events.packets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;


import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Sblock;

/**
 * Utility for spawning particles at an entity every tick.
 * 
 * @author Jikoo
 */
public class ParticleUtils {

	private static ParticleUtils instance;

	private HashMap<UUID, EntityParticleWrapper> entities;
	private BukkitTask task;

	private class EntityParticleWrapper {
		private UUID uuid;
		private boolean player;
		private HashSet<ParticleEffectWrapper> effects;
		public EntityParticleWrapper(Entity wrapped) {
			uuid = wrapped.getUniqueId();
			player = wrapped instanceof Player;
			effects = new HashSet<>();
		}
		public Entity getWrappedEntity() {
			if (player) {
				return Bukkit.getPlayer(uuid);
			}
			for (World world : Bukkit.getWorlds()) {
				for (Entity entity : world.getEntities()) {
					if (entity.getUniqueId().equals(uuid)) {
						return entity;
					}
				}
			}
			return null;
		}
		public void addParticle(ParticleEffectWrapper particle) {
			effects.add(particle);
		}
		public HashSet<ParticleEffectWrapper> removeEffect(Effect particleType) {
			for (Iterator<ParticleEffectWrapper> iterator = effects.iterator(); iterator.hasNext();) {
				if (iterator.next().getEffect() == particleType) {
					iterator.remove();
				}
			}
			return effects;
		}
		public HashSet<ParticleEffectWrapper> getEffects() {
			return effects;
		}
	}

	private ParticleUtils() {
		entities = new HashMap<>();
	}

	/**
	 * Add an Entity.
	 * 
	 * @param entity the Entity
	 */
	public void addEntity(Entity entity, ParticleEffectWrapper...effects) {
		EntityParticleWrapper wrapper;
		if (!entities.containsKey(entity.getUniqueId())) {
			wrapper = new EntityParticleWrapper(entity);
			entities.put(entity.getUniqueId(), wrapper);
		} else {
			wrapper = entities.get(entity.getUniqueId());
		}
		for (ParticleEffectWrapper effect : effects) {
			wrapper.addParticle(effect);
		}
		if (task == null) {
			task = new ParticleDisplayTask().runTaskTimer(Sblock.getInstance(), 0L, 1L);
		}
	}

	public void removeAllEffects(Entity entity) {
		entities.remove(entity.getUniqueId());
	}

	public void removeEffect(Entity entity, Effect particle) {
		if (!entities.containsKey(entity.getUniqueId())) {
			return;
		}
		if (entities.get(entity.getUniqueId()).removeEffect(particle).size() == 0) {
			entities.remove(entity.getUniqueId());
		}
	}

	private class ParticleDisplayTask extends BukkitRunnable {
		@Override
		public void run() {
			Iterator<Entry<UUID, EntityParticleWrapper>> iterator = entities.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<UUID, EntityParticleWrapper> entry = iterator.next();
				Entity entity = entry.getValue().getWrappedEntity();
				if (entity == null || entity.isDead()) {
					// If null, entity could just be unloaded, but that should be handled elsewhere.
					iterator.remove();
					continue;
				}
				Location location = entity.getLocation().add(0, .5, 0);
				entry.getValue().getEffects().forEach(effect -> {
					ProtocolLibrary.getProtocolManager().getEntityTrackers(entity).forEach(player -> {
						player.spigot().playEffect(location, effect.getEffect(), effect.getMaterial(),
								effect.getData(), effect.getOffsetX(), effect.getOffsetY(), effect.getOffsetZ(),
								effect.getSpeed(), effect.getParticleQuantity(), effect.getDisplayRadius());
					});
				});
			}

			if (entities.size() == 0) {
				cancel();
				task = null;
				return;
			}
		}
	}

	public static ParticleUtils getInstance() {
		if (instance == null) {
			instance = new ParticleUtils();
		}
		return instance;
	}
}
