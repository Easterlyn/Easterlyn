package com.easterlyn.micromodules.godules;

import com.easterlyn.micromodules.Godule;

/**
 * Interface defining methods for an aspect's "god" effect.
 * 
 * @author Jikoo
 */
public abstract class AspectEffect {

	private final Godule godule;

	private boolean enabled = false;

	public AspectEffect(Godule godule) {
		this.godule = godule;
	}

	public void onEnable() {
		if (enabled) {
			return;
		}
		enable();
		enabled = true;
	}

	protected abstract void enable();

	public void onDisable() {
		if (!enabled) {
			return;
		}
		disable();
		enabled = false;
	}

	protected abstract void disable();

	public boolean isEnabled() {
		return enabled;
	}

	protected Godule getGodule() {
		return this.godule;
	}

}
