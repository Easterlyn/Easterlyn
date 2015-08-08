package co.sblock.effects.effect.misc;

import org.bukkit.entity.LivingEntity;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.BehaviorPassive;

/**
 * Effect for indicating that the user has alchemized a computer into an object.
 * 
 * @author Jikoo
 */
public class EffectComputer extends Effect implements BehaviorPassive {

	public EffectComputer() {
		super(10, 1, 1, "Computer");
	}

	@Override
	public void applyEffect(LivingEntity entity, int level) {}

}
