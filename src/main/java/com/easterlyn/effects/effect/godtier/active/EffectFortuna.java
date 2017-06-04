package com.easterlyn.effects.effect.godtier.active;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAspect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * 
 * @author Jikoo
 */
public class EffectFortuna extends Effect implements BehaviorActive, BehaviorGodtier {

	public EffectFortuna(Easterlyn plugin) {
		super(plugin, 1000, 5, 5, "Fortuna");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		// GODTIER: should these be separate? Likely yes.
		return Arrays.asList(BlockBreakEvent.class, PlayerFishEvent.class, EntityDeathEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		// GODTIER
		
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Collections.singletonList(UserAspect.STARS);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAspect.STARS) {
			list.add(aspect.getColor() + "Fortuna");
		}
		list.add(ChatColor.WHITE + "Fortune just isn't enough.");
		list.add(ChatColor.GRAY + "+1 Fortune");
		return list;
	}

}
