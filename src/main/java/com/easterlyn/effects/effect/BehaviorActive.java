package com.easterlyn.effects.effect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import java.util.Collection;

/**
 * Interface declaring methods required for an active effect.
 *
 * @author Jikoo
 */
public interface BehaviorActive {

	/**
	 * Gets a Collection of Events during which this Effect triggers.
	 *
	 * @return the Collection
	 */
	Collection<Class<? extends Event>> getApplicableEvents();

	/**
	 * Handles an Event triggering this Effect.
	 *
	 * @param event the Event
	 * @param entity the LivingEntity involved
	 * @param level the level of the Effect on the LivingEntity
	 */
	void handleEvent(Event event, LivingEntity entity, int level);

}
