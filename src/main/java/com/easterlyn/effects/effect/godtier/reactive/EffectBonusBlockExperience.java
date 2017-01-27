package com.easterlyn.effects.effect.godtier.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.BehaviorReactive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAspect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExpEvent;

import net.md_5.bungee.api.ChatColor;

/**
 * Gives bonus experience for block actions.
 * 
 * @author Jikoo
 */
public class EffectBonusBlockExperience extends Effect implements BehaviorReactive, BehaviorGodtier {

	public EffectBonusBlockExperience(Easterlyn plugin) {
		super(plugin, 200, 5, 5, "Inquisitor");
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.MIND);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAspect.MIND) {
			list.add(aspect.getColor() + "Inquiring Mind");
		}
		list.add(ChatColor.WHITE + "Discover more.");
		list.add(ChatColor.GRAY + "Gain bonus experience from blocks.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(BlockExpEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		BlockExpEvent exp = (BlockExpEvent) event;

		exp.setExpToDrop((int) (exp.getExpToDrop() * (level * .25 + 1)));
	}

}
