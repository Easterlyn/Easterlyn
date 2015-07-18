package co.sblock.effects.effect.godtier;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;
import co.sblock.effects.effect.EffectBehaviorCooldown;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftAnimals;

import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Heart godtier active effect.
 * 
 * @author Dublekfx, Jikoo
 */
public class EffectHeartActive extends Effect implements EffectBehaviorActive, EffectBehaviorCooldown {

	public EffectHeartActive() {
		super(Integer.MAX_VALUE, 1, 1, "HEART::ACTIVE");
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
	public void handleEvent(Event event, Player player, int level) {
		PotionEffect potEffect = new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 1);
		player.addPotionEffect(potEffect, true);
		player.getWorld().playEffect(player.getLocation().add(0, 1, 0), org.bukkit.Effect.HEART, 0);
		for (Entity e : player.getNearbyEntities(8, 8, 8)) {
			Location loc;
			if (e instanceof Animals) {
				Animals animal = (Animals) e;
				if (animal.isAdult()) {
					loc = animal.getLocation().add(0, 1, 0);
					animal.setBreed(true);

					NBTTagCompound tag = new NBTTagCompound();
					((CraftAnimals) animal).getHandle().b(tag);
					tag.setInt("InLove", 600);
					((CraftAnimals) animal).getHandle().a(tag);
				} else {
					animal.setAge(animal.getAge() + 20 * 60 * 5);
					continue;
				}
			} else if (e instanceof Player) {
				((Player) e).addPotionEffect(potEffect, true);
				loc = ((Player) e).getLocation().add(0, 1, 0);
			} else {
				continue;
			}
			loc.getWorld().playEffect(loc, org.bukkit.Effect.HEART, 0);
		}
	}

}
