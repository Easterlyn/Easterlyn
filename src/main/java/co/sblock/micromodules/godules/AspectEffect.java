package co.sblock.micromodules.godules;

/**
 * Interface defining methods for an aspect's "god" effect.
 * 
 * @author Jikoo
 */
public abstract class AspectEffect {

	private boolean enabled = false;

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

}
