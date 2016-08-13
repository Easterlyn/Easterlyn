package co.sblock.effects.effect.godtier.passive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;
import co.sblock.utilities.Potions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

/**
 * Effect granting the user the absorption PotionEffect every minute. Due to the nature of
 * absorption granting free health, this has a longer duration and cooldown than most other passive
 * effects.
 * 
 * @author Jikoo
 */
public class EffectAbsorption extends Effect implements BehaviorCooldown, BehaviorGodtier,
		BehaviorPassive {

	public EffectAbsorption(Sblock plugin) {
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
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.HEART, UserAspect.TIME);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAspect.HEART) {
			list.add(aspect.getColor() + "Hale and Hearty");
		} else if (aspect == UserAspect.TIME) {
			list.add(aspect.getColor() + "Futureproof");
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
