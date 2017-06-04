package com.easterlyn.effects.effect;

/**
 * Interface declaring cooldown-related methods for an Effect.
 *
 * @author Jikoo
 */
public interface BehaviorCooldown {

	/**
	 * Gets the name to use for this Effect's cooldown.
	 *
	 * @return the name
	 */
	String getCooldownName();

	/**
	 * Gets the cooldown's duration in milliseconds.
	 *
	 * @return the duration
	 */
	long getCooldownDuration();

}
