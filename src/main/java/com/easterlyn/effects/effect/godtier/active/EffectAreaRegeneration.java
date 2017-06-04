package com.easterlyn.effects.effect.godtier.active;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.BehaviorCooldown;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAspect;
import com.easterlyn.utilities.Potions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Applies regeneration to all entities in an 8 block radius.
 * 
 * @author Dublek, Jikoo
 */
public class EffectAreaRegeneration extends Effect implements BehaviorActive, BehaviorCooldown, BehaviorGodtier {

	public EffectAreaRegeneration(Easterlyn plugin) {
		super(plugin, Integer.MAX_VALUE, 5, 5, "Team Regeneration");
	}

	@Override
	public String getCooldownName() {
		return "Effect:AreaRegeneration";
	}

	@Override
	public long getCooldownDuration() {
		return 60000;
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Collections.singletonList(UserAspect.ENERGY);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAspect.ENERGY) {
			list.add(aspect.getColor() + "Take Heart");
		}
		list.add(ChatColor.WHITE + "Fortify your allies.");
		list.add(ChatColor.GRAY + "Sneak and right click to heal nearby entities.");
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
		PotionEffect potEffect = new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 1);
		Potions.applyIfBetter(player, potEffect);
		for (Entity near : player.getNearbyEntities(8, 8, 8)) {
			if (near instanceof LivingEntity) {
				Potions.applyIfBetter((LivingEntity) near, potEffect);
			}
		}
	}

}
