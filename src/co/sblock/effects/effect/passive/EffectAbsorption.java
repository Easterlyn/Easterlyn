package co.sblock.effects.effect.passive;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorCooldown;
import co.sblock.effects.effect.EffectBehaviorPassive;
import co.sblock.utilities.general.Potions;

/**
 * Effect granting the user the absorption PotionEffect every minute. Due to the nature of
 * absorption granting free health, this has a longer duration and cooldown than most other passive
 * effects.
 * 
 * @author Jikoo
 */
public class EffectAbsorption extends Effect implements EffectBehaviorPassive,
		EffectBehaviorCooldown {

	public EffectAbsorption() {
		super(500, 4, 20, "Absorbant", "HEART::PASSIVE");
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
	public void applyEffect(Player player, int level) {
		if (level < 1) {
			level = 1;
		}
		Potions.applyIfBetter(player, new PotionEffect(PotionEffectType.ABSORPTION, 1200, level - 1));
	}
}
