package co.sblock.effects.effect;

import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

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
	public Collection<Class<? extends Event>> getApplicableEvents();

	/**
	 * Handles an Event triggering this Effect.
	 * 
	 * @param event the Event
	 * @param entity the LivingEntity involved
	 * @param level the level of the Effect on the LivingEntity
	 */
	public void handleEvent(Event event, LivingEntity entity, int level);
}
