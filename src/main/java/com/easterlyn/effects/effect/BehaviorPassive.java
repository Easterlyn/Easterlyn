package com.easterlyn.effects.effect;

import org.bukkit.entity.LivingEntity;

/**
 * Interface declaring methods required for a passive effect.
 *
 * @author Jikoo
 */
public interface BehaviorPassive {

	/**
	 * Applies the Effect to the given LivingEntity.
	 *
	 * @param entity the LivingEntity
	 */
	void applyEffect(LivingEntity entity, int level);

}
