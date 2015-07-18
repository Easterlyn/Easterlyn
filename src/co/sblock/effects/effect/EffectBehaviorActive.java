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

	public Collection<Class<? extends Event>> getApplicableEvents();

	public void handleEvent(Event event, Player player, int level);
}
