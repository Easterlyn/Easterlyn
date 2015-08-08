package co.sblock.effects.effect.passive;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.Effect;
import co.sblock.utilities.general.Potions;

/**
 * Effect for passively granting the jump PotionEffect.
 * 
 * @author Jikoo
 */
public class EffectJump  extends Effect implements BehaviorPassive, BehaviorCooldown {

	public EffectJump() {
		super(500, 2, 10, "Jump");
	}

	@Override
	public String getCooldownName() {
		return "Effect:Jump";
	}

	@Override
	public long getCooldownDuration() {
		return 5000;
	}

	@Override
	public void applyEffect(LivingEntity entity, int level) {
		if (level < 1) {
			level = 1;
		}
		int duration = entity instanceof Player ? 200 : Integer.MAX_VALUE;
		Potions.applyIfBetter(entity, new PotionEffect(PotionEffectType.JUMP, duration, level - 1));
	}

}
