package com.easterlyn.effects.effect.misc;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorPassive;
import com.easterlyn.effects.effect.Effect;

import org.bukkit.entity.LivingEntity;

/**
 * Effect for indicating that the user has alchemized a computer into an object.
 * 
 * @author Jikoo
 */
public class EffectComputer extends Effect implements BehaviorPassive {

	public EffectComputer(Easterlyn plugin) {
		super(plugin, 10, 1, 1, "Computer");
	}

	@Override
	public void applyEffect(LivingEntity entity, int level) {}

}
