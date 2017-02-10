package com.easterlyn.effects.effect.godtier.passive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorCooldown;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.BehaviorPassive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAspect;
import com.easterlyn.utilities.Potions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

/**
 * Applies Speed and Haste to nearby entities, not the entity with the actual Effect.
 * 
 * @author Jikoo
 */
public class EffectTeamAcceleration extends Effect implements BehaviorCooldown, BehaviorGodtier,
		BehaviorPassive {

	public EffectTeamAcceleration(Easterlyn plugin) {
		super(plugin, 1000, 3, 20, "Team Acceleration");
	}

	@Override
	public String getCooldownName() {
		return "Effect:TeamAcceleration";
	}

	@Override
	public long getCooldownDuration() {
		return 5000;
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.WIND);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAspect.WIND) {
			list.add(aspect.getColor() + "Wind Beneath Their Wings");
		}
		list.add(ChatColor.WHITE + "Assist your comrades.");
		list.add(ChatColor.GRAY + "Buff nearby entities with Speed and Haste.");
		return list;
	}

	@Override
	public void applyEffect(LivingEntity entity, int level) {
		if (level < 1) {
			level = 1;
		}
		PotionEffect potSpeed = new PotionEffect(PotionEffectType.SPEED, 200, 1);
		PotionEffect potHaste = new PotionEffect(PotionEffectType.FAST_DIGGING, 200, 1);
		for (Entity near : entity.getNearbyEntities(8, 8, 8)) {
			if (near instanceof LivingEntity) {
				LivingEntity livingNear = (LivingEntity) near;
				Potions.applyIfBetter(livingNear, potSpeed);
				Potions.applyIfBetter(livingNear, potHaste);
			}
		}
	}

}
