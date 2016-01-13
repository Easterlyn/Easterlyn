package co.sblock.events.packets;

import org.bukkit.Effect;
import org.bukkit.Material;

/**
 * Wrapper for all parameters required to send a Player a decent looking particle effect.
 * 
 * @author Jikoo
 */
public class ParticleEffectWrapper {
	private final Effect effect;
	private final float offsetX, offsetY, offsetZ, speed;
	private final int material, data, quantity, radius;
	@SuppressWarnings("deprecation")
	public ParticleEffectWrapper(Effect effect, Material material, Short durability, float offsetX, float offsetY, float offsetZ, float speed, int quantity, int displayRadius) {
		this.effect = effect;
		this.material = material != null ? material.getId() : 0;
		this.data = durability != null ? durability : 0;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.quantity = quantity;
		this.radius = displayRadius;
	}
	public ParticleEffectWrapper(Effect effect, int particles) {
		this(effect, null, null, .5F, .5F, .5F, 1F, particles, 32);
	}
	public Effect getEffect() {
		return effect;
	}
	public int getMaterial() {
		return material;
	}
	public int getData() {
		return data;
	}
	public float getOffsetX() {
		return offsetX;
	}
	public float getOffsetY() {
		return offsetY;
	}
	public float getOffsetZ() {
		return offsetZ;
	}
	public float getSpeed() {
		return speed;
	}
	public int getParticleQuantity() {
		return quantity;
	}
	public int getDisplayRadius() {
		return radius;
	}
}
