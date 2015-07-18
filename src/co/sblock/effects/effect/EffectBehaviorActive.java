package co.sblock.effects.effect;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Interface declaring methods required for an active effect.
 * 
 * @author Jikoo
 */
public interface EffectBehaviorActive {

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
	 * @param player the Player involved
	 * @param level the level of the Effect on the Player
	 */
	public void handleEvent(Event event, Player player, int level);
}
