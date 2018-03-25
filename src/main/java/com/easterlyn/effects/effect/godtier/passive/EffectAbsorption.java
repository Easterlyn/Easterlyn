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
 * Effect granting the user the absorption PotionEffect every minute. Due to the nature of
 * absorption granting free health, this has a longer duration and cooldown than most other passive
 * effects.
 *
 * @author Jikoo
 */
public class EffectAbsorption extends Effect implements BehaviorCooldown, BehaviorGodtier,
		BehaviorPassive {

	public EffectAbsorption(Easterlyn plugin) {
		super(plugin, 1000, 3, 20, "Absorbant");
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
	public Collection<UserAffinity> getAffinity() {
		return Collections.singletonList(UserAffinity.LIGHT);
	}

	@Override
	public List<String> getDescription(UserAffinity aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAffinity.LIGHT) {
			list.add(aspect.getColor() + "Cloak of Stars");
		}
		list.add(ChatColor.WHITE + "An apple a day keeps the doctor away.");
		list.add(ChatColor.GRAY + "Gain extra life.");
		return list;
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
