package co.sblock.effects.effect.godtier;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;

/**
 * 
 * 
 * @author Jikoo
 */
public class EffectLightPassive extends Effect implements EffectBehaviorActive {

	public EffectLightPassive() {
		super(1000, 5, 5, "Luck", "LIGHT::ACTIVE");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		// TODO should these be separate? Likely yes.
		return Arrays.asList(BlockBreakEvent.class, PlayerFishEvent.class, EntityDeathEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		// TODO
		
	}

}
