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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import net.md_5.bungee.api.ChatColor;

/**
 * Faster flight.
 * 
 * @author Jikoo
 */
public class EffectFasterFlight extends Effect implements BehaviorGodtier, BehaviorReactive {

	public EffectFasterFlight(Easterlyn plugin) {
		super(plugin, 500, 5, 10, "Flighty");
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.BREATH);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAspect.BREATH) {
			list.add(aspect.getColor() + "Heavenly Parasail");
		}
		list.add(ChatColor.WHITE + "The winds are on your side.");
		list.add(ChatColor.GRAY + "Fly faster.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(PlayerToggleFlightEvent.class, PlayerQuitEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		Player player = (Player) entity;
		if (event instanceof PlayerQuitEvent && !player.isFlying()) {
			return;
		}
		float toSet;
		if (event instanceof PlayerToggleFlightEvent
				&& ((PlayerToggleFlightEvent) event).isFlying()) {
			toSet = player.getFlySpeed() + level * 0.1F;
		} else {
			toSet = player.getFlySpeed() - level * 0.1F;
		}
		// 0.1F is normal flight speed, 1F is max flight speed.
		player.setFlySpeed(Math.max(0.1F, Math.min(toSet, 1F)));
	}

}
