package com.easterlyn.micromodules.godules;

import com.easterlyn.micromodules.Godule;

/**
 * Effect for the Breath "god" entering the game.
 * <p>
 * Mostly a placeholder, it's more efficient to use the existing player kill detection system.
 * 
 * @author Jikoo
 */
public class BreathEffect extends AspectEffect {

	public BreathEffect(Godule godule) {
		super(godule);
	}

	@Override
	protected void enable() {}

	@Override
	protected void disable() {}

}
