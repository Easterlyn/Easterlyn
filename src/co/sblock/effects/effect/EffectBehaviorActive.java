package co.sblock.effects.effect;

import org.bukkit.event.Event;

/**
 * Interface declaring methods required for an active effect.
 * 
 * @author Jikoo
 */
public interface EffectBehaviorActive {

	public Class<? extends Event>[] getApplicableEvents();

	public void handleEvent(Class<? extends Event> event);
}
