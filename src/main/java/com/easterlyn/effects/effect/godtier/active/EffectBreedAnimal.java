package com.easterlyn.effects.effect.godtier.active;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.BehaviorCooldown;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAffinity;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftAnimals;
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
 * Heart godtier active effect. Set animals breeding if nearby and adult, speed up the growth of
 * babies, and apply a regeneration PotionEffect to all nearby entities.
 *
 * @author Dublekfx, Jikoo
 */
public class EffectBreedAnimal extends Effect implements BehaviorActive, BehaviorCooldown, BehaviorGodtier {

	public EffectBreedAnimal(Easterlyn plugin) {
		super(plugin, Integer.MAX_VALUE, 5, 5, "Breeding");
	}

	@Override
	public String getCooldownName() {
		return "Effect:Breeding";
	}

	@Override
	public long getCooldownDuration() {
		return 60000;
	}

	@Override
	public Collection<UserAffinity> getAffinity() {
		return Collections.singletonList(UserAffinity.LIFE);
	}

	@Override
	public List<String> getDescription(UserAffinity aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAffinity.LIFE) {
			list.add(aspect.getColor() + "Instant Springtime");
		}
		list.add(ChatColor.WHITE + "Love is in the air.");
		list.add(ChatColor.GRAY + "Sneak and right click to breed nearby animals.");
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
			if (animal.isAdult()) {
				animal.setBreed(true);

				NBTTagCompound tag = new NBTTagCompound();
				((CraftAnimals) animal).getHandle().b(tag);
				tag.setInt("InLove", 600);
				((CraftAnimals) animal).getHandle().a(tag);
			}
		}
	}

}
