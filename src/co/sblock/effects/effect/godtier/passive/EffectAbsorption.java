package co.sblock.effects.effect.godtier.passive;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;
import co.sblock.utilities.general.Potions;

/**
 * Effect granting the user the absorption PotionEffect every minute. Due to the nature of
 * absorption granting free health, this has a longer duration and cooldown than most other passive
 * effects.
 * 
 * @author Jikoo
 */
public class EffectAbsorption extends Effect implements BehaviorCooldown, BehaviorGodtier,
		BehaviorPassive {

	public EffectAbsorption() {
		super(1000, 3, 20, "Absorbant");
	}

	@Override
	public String getCooldownName() {
		return "Effect:Absorption";
	}

	@Override
	public long getCooldownDuration() {
		return 55000;
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.HEART, UserAspect.TIME);
	}

	@Override
	public String getName(UserAspect aspect) {
		switch (aspect) {
		case HEART:
			return "Hale and Hearty";
		case TIME:
			return "Futureproof";
		default:
			return "If you are reading this, please report it.";
		}
	}

	@Override
	public String getDescription(UserAspect aspect) {
		switch (aspect) {
		case HEART:
		case TIME:
			return "Gain extra life!";
		default:
			return "If you are reading this, please report it.";
		}
	}

	@Override
	public void applyEffect(LivingEntity entity, int level) {
		if (level < 1) {
			level = 1;
		}
		int duration = entity instanceof Player ? 1200 : Integer.MAX_VALUE;
		Potions.applyIfBetter(entity, new PotionEffect(PotionEffectType.ABSORPTION, duration, level - 1));
	}

}
