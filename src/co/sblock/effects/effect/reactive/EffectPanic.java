package co.sblock.effects.effect.reactive;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorCooldown;
import co.sblock.effects.effect.EffectBehaviorReactive;
import co.sblock.utilities.general.Potions;

/**
 * Panic effect. When damaged, player may receive a speed boost and brief regeneration or slightly
 * reduced damage taken.
 * 
 * @author Jikoo
 */
public class EffectPanic extends Effect implements EffectBehaviorReactive, EffectBehaviorCooldown {

	public EffectPanic() {
		super(200, 5, 20, "Panic", "Adrenaline");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(EntityDamageEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		if (level > getMaxTotalLevel()) {
			level = getMaxTotalLevel();
		}

		EntityDamageEvent ede = (EntityDamageEvent) event;

		if (ede.getFinalDamage() > .5 && ede.getFinalDamage() > entity.getHealth() && Math.random() * 100 < level * 3) {
			// 10% damage reduction
			ede.setDamage(ede.getDamage() * .9);
		}

		double remainingHealth = entity.getHealth() - ede.getFinalDamage();
		if (remainingHealth < 0 || remainingHealth > level) {
			return;
		}

		level = level / 6;
		if (Math.random() < .75) {
			Potions.applyIfBetter(entity, new PotionEffect(PotionEffectType.SPEED, 80 * (level + 1), level));
		}
		level = level / 2;
		if (Math.random() < .3) {
			Potions.applyIfBetter(entity, new PotionEffect(PotionEffectType.REGENERATION, 40 / (level + 1), level));
		}
	}

	@Override
	public String getCooldownName() {
		return "Effect:Panic";
	}

	@Override
	public long getCooldownDuration() {
		return 5000L;
	}

}
