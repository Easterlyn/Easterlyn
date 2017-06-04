package com.easterlyn.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.Effect;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * In essence, the opposite of knockback.
 * 
 * @author Jikoo
 */
public class EffectPull extends Effect implements BehaviorActive {

	public EffectPull(Easterlyn plugin) {
		super(plugin, 200, 2, 2, "Pull");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Collections.singletonList(EntityDamageByEntityEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		EntityDamageByEntityEvent dmgEvent = (EntityDamageByEntityEvent) event;
		Entity damaged = dmgEvent.getEntity();
		if (!(damaged instanceof LivingEntity)) {
			return;
		}
		damaged.setVelocity(entity.getLocation().toVector().subtract(damaged.getLocation().toVector()).multiply(level * 0.35));
	}

}
