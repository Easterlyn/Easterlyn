package co.sblock.fx;

import java.util.ArrayList;

import net.minecraft.server.v1_8_R1.NBTTagCompound;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftAnimals;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.Sblock;
import co.sblock.users.OnlineUser;

public class FXGodtierHeartActive extends SblockFX {

	@SuppressWarnings("unchecked")
	public FXGodtierHeartActive() {
		super("FXGodtierHeartActive", false, 0, 1000 * 60, PlayerInteractEvent.class,PlayerInteractEntityEvent.class);
	}

	@Override
	protected void getEffect(OnlineUser user, Event event) {
		Sblock.getLog().info("Applying Godtier Active Effect");
		ArrayList<Entity> nearbyEntities = (ArrayList<Entity>) user.getPlayer().getNearbyEntities(8, 8, 8);
		PotionEffect potEffect = new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 1);
		user.getPlayer().addPotionEffect(potEffect, true);
		Location loc = user.getPlayer().getLocation();
		loc.setY(loc.getY() + 1);
		user.getPlayer().getWorld().playEffect(user.getPlayer().getLocation(), Effect.HEART, 0);
		user.getPlayer().getWorld().playEffect(loc, Effect.HEART, 0);
		for(Entity e : nearbyEntities) {
			Sblock.getLog().info(e.getType() + "");
			if(e instanceof Animals) {
				Animals animal = (Animals) e;
				if(animal.isAdult()) {
					loc = animal.getLocation();
					loc.setY(loc.getY() + 1);
					animal.setBreed(true);
					
					NBTTagCompound tag = new NBTTagCompound();
					((CraftAnimals)animal).getHandle().b(tag);
					tag.setInt("InLove", 600);
					((CraftAnimals)animal).getHandle().a(tag);
					
					animal.getWorld().playEffect(animal.getLocation(), Effect.HEART, 0);
					animal.getWorld().playEffect(loc, Effect.HEART, 0);
					Sblock.getLog().info("Breeding");
					Sblock.getLog().info(animal.getAge() + "");
				}
				else {
					animal.setAge(animal.getAge() + 20 * 60 * 5);
					Sblock.getLog().info(animal.getAge() + "");
				}
			}
			else if(e.getType() == EntityType.PLAYER) {
				((Player) e).addPotionEffect(potEffect, true);
				loc = ((Player) e).getLocation();
				loc.setY(loc.getY() + 1);
				((Player) e).getWorld().playEffect(((Player) e).getLocation(), Effect.HEART, 0);
				((Player) e).getWorld().playEffect(loc, Effect.HEART, 0);
			}
		}
	}

	@Override
	public void removeEffect(OnlineUser user) {
	}

}
