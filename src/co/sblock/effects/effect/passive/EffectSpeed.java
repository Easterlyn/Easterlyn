package co.sblock.effects.effect.passive;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorCooldown;
import co.sblock.effects.effect.EffectBehaviorPassive;
import co.sblock.users.OnlineUser;

/**
 * Effect for passively granting the speed potion effect.
 * 
 * @author Jikoo
 */
public class EffectSpeed extends Effect implements EffectBehaviorPassive, EffectBehaviorCooldown {

	public EffectSpeed(int cost, int maximumLevel, int maximumCombinedLevel, String[] names) {
		super(500, 2, 10, "Speed");
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCooldownName() {
		return "Effect:Speed";
	}

	@Override
	public long getCooldownDuration() {
		return 7000;
	}

	@Override
	public void applyEffect(OnlineUser user) {
		PotionEffect potEffect = new PotionEffect(PotionEffectType.SPEED, 200, user.getEffectLevel(this));
		user.getPlayer().addPotionEffect(potEffect, true);
	}

	@Override
	public void removeEffect(OnlineUser user) {
		// Due to the short duration of the effect, it will expire on its own.
	}

}
