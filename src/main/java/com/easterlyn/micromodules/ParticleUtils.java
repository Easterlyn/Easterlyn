package com.easterlyn.micromodules;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.easterlyn.Easterlyn;
import com.easterlyn.events.packets.ParticleEffectWrapper;
import com.easterlyn.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Utility for spawning particles at an entity every tick.
 * 
 * @author Jikoo
 */
public class ParticleUtils extends Module {

	private final HashMap<UUID, EntityParticleWrapper> entities;

	private BukkitTask task;

	public ParticleUtils(Easterlyn plugin) {
		super(plugin);
		entities = new HashMap<>();
	}

	private class EntityParticleWrapper {
		private final UUID uuid;
		private final boolean player;
		private final HashSet<ParticleEffectWrapper> effects;

		private EntityParticleWrapper(Entity wrapped) {
			uuid = wrapped.getUniqueId();
			player = wrapped instanceof Player;
			effects = new HashSet<>();
		}

		private Entity getWrappedEntity() {
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

		private void addParticle(ParticleEffectWrapper particle) {
			effects.add(particle);
		}

		private HashSet<ParticleEffectWrapper> removeEffect(Particle particleType) {
			effects.removeIf(particleEffectWrapper -> particleEffectWrapper.getParticle() == particleType);
			return effects;
		}
		public HashSet<ParticleEffectWrapper> getEffects() {
			return effects;
		}
	}

	@Override
	protected void onEnable() { }

	@Override
	protected void onDisable() { }

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
			task = new ParticleDisplayTask().runTaskTimer(getPlugin(), 0L, 1L);
		}
	}

	public void removeAllEffects(Entity entity) {
		entities.remove(entity.getUniqueId());
	}

	public void removeEffect(Entity entity, Particle particle) {
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
				ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
				entry.getValue().getEffects().forEach(effect ->
						protocolManager.getEntityTrackers(entity).forEach(player ->
								player.spawnParticle(effect.getParticle(), location.getX(), location.getY(),
										location.getZ(), effect.getParticleQuantity(), effect.getOffsetX(),
										effect.getOffsetY(), effect.getOffsetZ(), effect.getSpeed(),
										effect.getData())));
			}

			if (entities.size() == 0) {
				cancel();
				task = null;
			}
		}
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public String getName() {
		return "Particles";
	}

}
