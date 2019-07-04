package com.easterlyn.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

/**
 * A utility for common potion or effect-related methods.
 * 
 * @author Jikoo
 */
public class PotionUtil {

	/**
	 * Applies the given PotionEffect to an Entity provided the effect is not present or has a
	 * higher amplifier or duration than the one currently present.
	 * 
	 * @param entity the Entity
	 * @param effect the PotionEffect
	 */
	public static void applyIfBetter(LivingEntity entity, PotionEffect effect) {
		if (!entity.hasPotionEffect(effect.getType())) {
			entity.addPotionEffect(effect);
			return;
		}
		for (PotionEffect current : entity.getActivePotionEffects()) {
			if (!current.getType().equals(effect.getType())) {
				continue;
			}
			if (current.getAmplifier() > effect.getAmplifier()) {
				return;
			}
			if (current.getDuration() > effect.getDuration() && current.getAmplifier() == effect.getAmplifier()) {
				return;
			}
			entity.addPotionEffect(effect, true);
			return;
		}
	}

}
