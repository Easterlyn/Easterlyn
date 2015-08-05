package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;

/**
 * In essence, the opposite of knockback.
 * 
 * @author Jikoo
 */
public class EffectPull extends Effect implements EffectBehaviorActive {

	public EffectPull() {
		super(200, 2, 2, "Pull");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(EntityDamageByEntityEvent.class);
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
