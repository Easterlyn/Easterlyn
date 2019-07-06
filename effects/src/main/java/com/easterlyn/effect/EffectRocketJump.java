package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Per request, the ever-popular CrotchRocket.
 *
 * @author Jikoo
 */
public class EffectRocketJump extends Effect {

	public EffectRocketJump(EasterlynEffects plugin) {
		super(plugin, "Crotchrocket", EquipmentSlots.HELD, 300, 1, 1);
	}

	@Override
	public void applyEffect(@NotNull LivingEntity entity, int level, Event event) {
		if (!(event instanceof PlayerInteractEvent)) {
			return;
		}
		if (entity.getMetadata(getName()).stream().anyMatch(value -> value.asLong() > System.currentTimeMillis())) {
			return;
		}
		entity.removeMetadata(getName(), getPlugin());
		entity.setMetadata(getName(), new FixedMetadataValue(getPlugin(), System.currentTimeMillis() + 1000));


		entity.setFallDistance(0);
		entity.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, entity.getLocation(), 1);

		final Firework firework = (Firework) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.FIREWORK);
		FireworkMeta fm = firework.getFireworkMeta();
		fm.setPower(4);
		firework.setFireworkMeta(fm);
		firework.addPassenger(entity);

		new BukkitRunnable() {

			private int count = 0;

			@Override
			public void run() {
				if (count > 39 || firework.getLocation().getY() > 255) {
					firework.remove();
					cancel();
					return;
				}
				++count;
				firework.setVelocity(new Vector(0, 1, 0));
				firework.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, firework.getLocation(), 5);
			}
		}.runTaskTimer(getPlugin(), 0L, 1L);
	}

}
