package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Essentially the opposite of knockback.
 *
 * @author Jikoo
 */
public class EffectPullEntity extends Effect {

  public EffectPullEntity(EasterlynEffects plugin) {
    super(plugin, "Pull", EquipmentSlots.TOOL, 200, 2, 2);
  }

  @Override
  public void applyEffect(@NotNull LivingEntity entity, int level, Event event) {
    if (!(event instanceof EntityDamageByEntityEvent)) {
      return;
    }
    EntityDamageByEntityEvent dmgEvent = (EntityDamageByEntityEvent) event;
    Entity damaged = dmgEvent.getEntity();
    if (!(damaged instanceof LivingEntity)) {
      return;
    }
    damaged.setVelocity(
        entity
            .getLocation()
            .toVector()
            .subtract(damaged.getLocation().toVector())
            .normalize()
            .multiply(level * 0.35));
  }
}
