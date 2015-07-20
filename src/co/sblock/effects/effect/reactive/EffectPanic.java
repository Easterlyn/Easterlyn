package co.sblock.effects.effect.reactive;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorCooldown;
import co.sblock.effects.effect.EffectBehaviorReactive;

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
	public void handleEvent(Event event, Player player, int level) {
		if (level > getMaxTotalLevel()) {
			level = getMaxTotalLevel();
		}

		EntityDamageEvent ede = (EntityDamageEvent) event;

		if (ede.getFinalDamage() > .5 && ede.getFinalDamage() > player.getHealth() && Math.random() * 100 < level * 3) {
			// 10% damage reduction
			ede.setDamage(ede.getDamage() * .9);
			return;
		}

		double remainingHealth = player.getHealth() - ede.getFinalDamage();
		if (remainingHealth < 0 || remainingHealth > level) {
			return;
		}

		level = (int) Math.ceil(level / 5D);
		if (Math.random() < .75) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80 * level, level));
		}
		level = (int) Math.ceil(level / 2D);
		if (Math.random() < .3) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40 / level, level));
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
