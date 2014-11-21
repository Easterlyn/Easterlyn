package co.sblock.events.packets;

import org.bukkit.Effect;

/**
 * Wrapper for a ParticleEffect and quantity.
 * 
 * @author Jikoo
 */
public class ParticleEffectWrapper {
	private final Effect effect;
	private final int particles;
	public ParticleEffectWrapper(Effect effect, int particles) {
		this.effect = effect;
		this.particles = particles;
	}
	public Effect getEffect() {
		return effect;
	}
	public int getParticles() {
		return particles;
	}
}
