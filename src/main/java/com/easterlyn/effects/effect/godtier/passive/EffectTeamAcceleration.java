package com.easterlyn.effects.effect.godtier.passive;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorCooldown;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.BehaviorPassive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAffinity;
import com.easterlyn.utilities.Potions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
	public Collection<UserAffinity> getAffinity() {
		return Collections.singletonList(UserAffinity.AIR);
	}

	@Override
	public List<String> getDescription(UserAffinity aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAffinity.AIR) {
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
		PotionEffect potSpeed = new PotionEffect(PotionEffectType.SPEED, 200, level);
		PotionEffect potHaste = new PotionEffect(PotionEffectType.FAST_DIGGING, 200, level);
		for (Entity near : entity.getNearbyEntities(8, 8, 8)) {
			if (near instanceof LivingEntity) {
				LivingEntity livingNear = (LivingEntity) near;
				Potions.applyIfBetter(livingNear, potSpeed);
				Potions.applyIfBetter(livingNear, potHaste);
			}
		}
	}

}
