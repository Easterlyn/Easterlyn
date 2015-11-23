package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.Effect;

/**
 * Try not to die!
 * 
 * @author Jikoo
 */
public class EffectPshoooot extends Effect implements BehaviorActive {

	public EffectPshoooot(Sblock plugin) {
		super(plugin, 350, 5, 10, "Pshoooot");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(PlayerInteractEvent.class);
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
