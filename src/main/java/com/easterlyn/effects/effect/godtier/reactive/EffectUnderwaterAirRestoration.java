package com.easterlyn.effects.effect.godtier.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.BehaviorReactive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAspect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityAirChangeEvent;

import net.md_5.bungee.api.ChatColor;

/**
 * 50% chance not to consume air when underwater.
 * 
 * @author Jikoo
 */
public class EffectUnderwaterAirRestoration extends Effect implements BehaviorGodtier, BehaviorReactive {

	public EffectUnderwaterAirRestoration(Easterlyn plugin) {
		super(plugin, 500, 1, 3, "Extra Air");
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Collections.singletonList(UserAspect.WIND);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAspect.WIND) {
			list.add(aspect.getColor() + "Reed Rebreather");
		}
		list.add(ChatColor.WHITE + "Breathe easy, breathe deep.");
		list.add(ChatColor.GRAY + "Chance of regaining air underwater.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Collections.singletonList(EntityAirChangeEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		if (Math.random() < .5) {
			((EntityAirChangeEvent) event).setCancelled(true);
		}
	}

}
