package co.sblock.events.packets;

import org.bukkit.Particle;

/**
 * Wrapper for all parameters required to send a Player a decent looking particle effect.
 * 
 * @author Jikoo
 */
public class ParticleEffectWrapper {

	private final Particle particle;
	private final float offsetX, offsetY, offsetZ, speed;
	private final int quantity, radius;
	private final Object data;

	public  ParticleEffectWrapper(Particle particle, Object data, float offsetX,
			float offsetY, float offsetZ, float speed, int quantity, int displayRadius) {
		this.particle = particle;
		this.data = data;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.quantity = quantity;
		this.radius = displayRadius;
	}

	public ParticleEffectWrapper(Particle particle, int particles) {
		this(particle, null, .5F, .5F, .5F, 1F, particles, 32);
	}

	public Particle getParticle() {
		return this.particle;
	}

	public Object getData() {
		return this.data;
	}

	public float getOffsetX() {
		return this.offsetX;
	}

	public float getOffsetY() {
		return this.offsetY;
	}

	public float getOffsetZ() {
		return this.offsetZ;
	}

	public float getSpeed() {
		return this.speed;
	}

	public int getParticleQuantity() {
		return this.quantity;
	}

	public int getDisplayRadius() {
		return this.radius;
	}

}
