package co.sblock.effects.effect.godtier.active;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;
import co.sblock.utilities.Potions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

/**
 * Applies regeneration to all entities in an 8 block radius.
 * 
 * @author Dublek, Jikoo
 */
public class EffectAreaRegeneration extends Effect implements BehaviorActive, BehaviorCooldown, BehaviorGodtier {

	public EffectAreaRegeneration(Sblock plugin) {
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
		return Arrays.asList(UserAspect.HEART);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAspect.HEART) {
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
