package com.easterlyn.effects.effect.active;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.Effect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collection;
import java.util.Collections;

/**
 * Try not to die!
 * 
 * @author Jikoo
 */
public class EffectPshoooot extends Effect implements BehaviorActive {

	public EffectPshoooot(Easterlyn plugin) {
		super(plugin, 350, 5, 10, "Pshoooot");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Collections.singletonList(PlayerInteractEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		entity.setFallDistance(0);
		if (entity.getLocation().getY() > 300) {
			return;
		}
		entity.setVelocity(entity.getLocation().getDirection().multiply(level + 2));
	}

}
