package co.sblock.effects.effect.godtier.active;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;
import co.sblock.utilities.general.Potions;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftAnimals;

import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Heart godtier active effect. Set animals breeding if nearby and adult, speed up the growth of
 * babies, and apply a regeneration PotionEffect to all nearby entities.
 * 
 * @author Dublekfx, Jikoo
 */
public class EffectAnimalBreeder extends Effect implements BehaviorActive, BehaviorCooldown, BehaviorGodtier {

	public EffectAnimalBreeder() {
		super(Integer.MAX_VALUE, 5, 5, "Breeding");
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
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.HEART);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		switch (aspect) {
		case HEART:
			list.add(aspect.getColor() + "TODO TODO SPLIT INTO MULTIPLE ACTIVES");
			break;
		default:
			break;
		}
		list.add(ChatColor.WHITE + "This is a placeholder description!");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(PlayerInteractEvent.class, PlayerInteractEntityEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		// TODO split into multiple effects
		Player player = (Player) entity;
		if (!player.isSneaking()) {
			return;
		}
		PotionEffect potEffect = new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 1);
		Potions.applyIfBetter(player, potEffect);
		player.getWorld().playEffect(player.getLocation().add(0, 1, 0), org.bukkit.Effect.HEART, 0);
		for (Entity near : player.getNearbyEntities(8, 8, 8)) {
			Location loc;
			if (near instanceof Animals) {
				Animals animal = (Animals) near;
				if (animal.isAdult()) {
					loc = animal.getLocation().add(0, 1, 0);
					animal.setBreed(true);

					NBTTagCompound tag = new NBTTagCompound();
					((CraftAnimals) animal).getHandle().b(tag);
					tag.setInt("InLove", 600);
					((CraftAnimals) animal).getHandle().a(tag);
				} else {
					animal.setAge(animal.getAge() + 20 * 60 * 5);
				}
			}
			if (near instanceof LivingEntity) {
				Potions.applyIfBetter((LivingEntity) near, potEffect);
				loc = ((Player) near).getLocation().add(0, 1, 0);
			} else {
				continue;
			}
			loc.getWorld().playEffect(loc, org.bukkit.Effect.HEART, 0);
		}
	}

}
