package co.sblock.effects.effect.godtier;

import java.util.Arrays;
import java.util.Collection;

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

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;
import co.sblock.effects.effect.EffectBehaviorCooldown;
import co.sblock.utilities.general.Potions;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftAnimals;

import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Heart godtier active effect. Set animals breeding if nearby and adult, speed up the growth of
 * babies, and apply a regeneration PotionEffect to all nearby entities.
 * 
 * @author Dublekfx, Jikoo
 */
public class EffectHeartActive extends Effect implements EffectBehaviorActive, EffectBehaviorCooldown {

	public EffectHeartActive() {
		super(Integer.MAX_VALUE, 5, 5, "HEART::ACTIVE");
	}

	@Override
	public String getCooldownName() {
		return "Effect:HeartActive";
	}

	@Override
	public long getCooldownDuration() {
		return 60000;
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
