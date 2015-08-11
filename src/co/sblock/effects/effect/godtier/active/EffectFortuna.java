package co.sblock.effects.effect.godtier.active;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;

/**
 * 
 * 
 * @author Jikoo
 */
public class EffectFortuna extends Effect implements BehaviorActive, BehaviorGodtier {

	public EffectFortuna() {
		super(1000, 5, 5, "Fortuna");
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

	@Override
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.LIGHT);
	}

	@Override
	public String getName(UserAspect aspect) {
		switch (aspect) {
		case LIGHT:
			return "Fortuna";
		default:
			return "If you are reading this, please report it.";
		}
	}

	@Override
	public String getDescription(UserAspect aspect) {
		switch (aspect) {
		case LIGHT:
			return "Fortune just isn't enough.";
		default:
			return "If you are reading this, please report it.";
		}
	}

}
