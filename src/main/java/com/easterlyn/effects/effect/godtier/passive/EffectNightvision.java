package com.easterlyn.effects.effect.godtier.passive;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorCooldown;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.BehaviorPassive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAffinity;
import com.easterlyn.utilities.Potions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Effect for passively granting the speed PotionEffect.
 *
 * @author Jikoo
 */
public class EffectNightvision extends Effect implements BehaviorCooldown, BehaviorGodtier,
		BehaviorPassive {

	public EffectNightvision(Easterlyn plugin) {
		super(plugin, 500, 2, 10, "Nightvision");
	}

	@Override
	public Collection<UserAffinity> getAffinity() {
		return Collections.singleton(UserAffinity.SHADOW);
	}

	@Override
	public List<String> getDescription(UserAffinity aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAffinity.SHADOW) {
			list.add(aspect.getColor() + "Cat's Eyes");
		}
		list.add(ChatColor.WHITE + "To understand the night, one must be the night.");
		list.add(ChatColor.GRAY + "See in the dark.");
		return list;
	}

	@Override
	public String getCooldownName() {
		return "Effect:Nightvision";
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
		Potions.applyIfBetter(entity, new PotionEffect(PotionEffectType.SPEED, duration, level - 1));
	}

}
