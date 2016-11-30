package co.sblock.events.packets;

import com.comphenix.protocol.wrappers.EnumWrappers.Particle;

import org.bukkit.Material;

/**
 * Wrapper for all parameters required to send a Player a decent looking particle effect.
 * 
 * @author Jikoo
 */
public class ParticleEffectWrapper {

	private final Particle particle;
	private final float offsetX, offsetY, offsetZ, speed;
	private final int material, quantity, radius;
	private final int[] data;

	@SuppressWarnings("deprecation")
	public ParticleEffectWrapper(Particle particle, Material material, float offsetX, float offsetY,
			float offsetZ, float speed, int quantity, int displayRadius, int... data) {
		this.particle = particle;
		this.material = material != null ? material.getId() : 0;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.quantity = quantity;
		this.radius = displayRadius;
		this.data = data;
	}

	public ParticleEffectWrapper(Particle particle, int particles) {
		this(particle, null, .5F, .5F, .5F, 1F, particles, 32);
	}

	public Particle getEffect() {
		return this.particle;
	}

	public int getMaterial() {
		return this.material;
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

	public int[] getData() {
		return this.data;
	}

}
