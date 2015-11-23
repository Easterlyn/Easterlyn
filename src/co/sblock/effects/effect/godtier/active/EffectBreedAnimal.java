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

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftAnimals;

import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Heart godtier active effect. Set animals breeding if nearby and adult, speed up the growth of
 * babies, and apply a regeneration PotionEffect to all nearby entities.
 * 
 * @author Dublekfx, Jikoo
 */
public class EffectBreedAnimal extends Effect implements BehaviorActive, BehaviorCooldown, BehaviorGodtier {

	public EffectBreedAnimal(Sblock plugin) {
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
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.HEART);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		switch (aspect) {
		case HEART:
			list.add(aspect.getColor() + "Instant Springtime");
			break;
		default:
			break;
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
