package com.easterlyn.effects.effect.godtier.active;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.BehaviorCooldown;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAffinity;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Speed up the growth of nearby animals.
 *
 * @author Dublek, Jikoo
 */
public class EffectAgeAnimal extends Effect implements BehaviorActive, BehaviorCooldown, BehaviorGodtier {

	public EffectAgeAnimal(Easterlyn plugin) {
		super(plugin, Integer.MAX_VALUE, 1, 1, "Fine Wine");
	}

	@Override
	public String getCooldownName() {
		return "Effect:AgeAnimal";
	}

	@Override
	public long getCooldownDuration() {
		return 60000;
	}

	@Override
	public Collection<UserAffinity> getAffinity() {
		return Collections.singletonList(UserAffinity.TIME);
	}

	@Override
	public List<String> getDescription(UserAffinity aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAffinity.TIME) {
			list.add(aspect.getColor() + "Before You Know It");
		}
		list.add(ChatColor.WHITE + "Speed up young animals' growth.");
		list.add(ChatColor.GRAY + "Sneak and right click near baby animals.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(PlayerInteractEvent.class, PlayerInteractEntityEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		Player player = (Player) entity;
		if (!player.isSneaking()) {
			return;
		}
		for (Entity near : player.getNearbyEntities(8, 8, 8)) {
			if (!(near instanceof Animals)) {
				continue;
			}
			Animals animal = (Animals) near;
			if (!animal.isAdult()) {
				animal.setAge(animal.getAge() + 20 * 60);
			}
		}
	}

}
