package co.sblock.effects.effect.godtier.passive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;
import co.sblock.utilities.Potions;

import net.md_5.bungee.api.ChatColor;

/**
 * Applies Speed and Haste to nearby entities, not the entity with the actual Effect.
 * 
 * @author Jikoo
 */
public class EffectTeamAcceleration extends Effect implements BehaviorCooldown, BehaviorGodtier,
		BehaviorPassive {

	public EffectTeamAcceleration() {
		super(1000, 3, 20, "Team Acceleration");
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
		return Arrays.asList(UserAspect.BREATH);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		switch (aspect) {
		case BREATH:
			list.add(aspect.getColor() + "Wind Beneath Their Wings");
			break;
		default:
			break;
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
