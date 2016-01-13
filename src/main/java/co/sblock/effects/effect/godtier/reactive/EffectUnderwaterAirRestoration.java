package co.sblock.effects.effect.godtier.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.LivingEntity;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;

import net.md_5.bungee.api.ChatColor;

/**
 * Periodic chances of having 15% of maximum air added while under max.
 * 
 * @author Jikoo
 */
public class EffectUnderwaterAirRestoration extends Effect implements BehaviorGodtier, BehaviorPassive {

	public EffectUnderwaterAirRestoration(Sblock plugin) {
		super(plugin, 500, 1, 3, "Extra Air");
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
			list.add(aspect.getColor() + "Reed Rebreather");
			break;
		default:
			break;
		}
		list.add(ChatColor.WHITE + "Breathe easy, breathe deep.");
		list.add(ChatColor.GRAY + "Chance of regaining air underwater.");
		return list;
	}

	@Override
	public void applyEffect(LivingEntity entity, int level) {
		for (int i = 0; i < level; i++) {
			if (entity.getRemainingAir() >= entity.getMaximumAir()) {
				break;
			}
			if (Math.random() < .5) {
				entity.setRemainingAir(entity.getRemainingAir() + entity.getMaximumAir() / 15);
			}
		}
	}

}
