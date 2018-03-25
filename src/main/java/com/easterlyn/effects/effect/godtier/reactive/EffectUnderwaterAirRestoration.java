package com.easterlyn.effects.effect.godtier.reactive;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.BehaviorReactive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAffinity;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityAirChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
	public Collection<UserAffinity> getAffinity() {
		return Collections.singletonList(UserAffinity.WATER);
	}

	@Override
	public List<String> getDescription(UserAffinity aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAffinity.WATER) {
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
		if (ThreadLocalRandom.current().nextDouble() < .5) {
			((EntityAirChangeEvent) event).setCancelled(true);
		}
	}

}
