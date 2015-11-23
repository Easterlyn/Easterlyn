package co.sblock.effects.effect.godtier.active;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;

import net.md_5.bungee.api.ChatColor;

/**
 * Speed up the growth of nearby animals.
 * 
 * @author Dublek, Jikoo
 */
public class EffectAgeAnimal extends Effect implements BehaviorActive, BehaviorCooldown, BehaviorGodtier {

	public EffectAgeAnimal(Sblock plugin) {
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
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.HEART, UserAspect.TIME);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		switch (aspect) {
		case HEART:
			list.add(aspect.getColor() + "Hearty Breakfast");
			break;
		case TIME:
			list.add(aspect.getColor() + "Before You Know It");
			break;
		default:
			break;
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
