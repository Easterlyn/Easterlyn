package com.easterlyn.micromodules;

import com.easterlyn.Easterlyn;
import com.easterlyn.module.Module;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import net.minecraft.server.v1_11_R1.Explosion;

import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;

/**
 * A Module for creating falling explosive spheres.
 *
 * @author Dublek, Jikoo
 */
public class Meteors extends Module {

	public Meteors(Easterlyn plugin) {
		super(plugin);
	}

	/**
	 * @see Module#onEnable()
	 */
	@Override
	public void onEnable() { }

	/**
	 * @see Module#onDisable()
	 */
	@Override
	public void onDisable() { }

	/**
	 * Deals with EntityChangeBlockEvents to keep all related NMS/OBC access in one area.
	 *
	 * @param event the EntityChangeBlockEvent
	 */
	public void handlePotentialMeteorite(EntityChangeBlockEvent event) {
		net.minecraft.server.v1_11_R1.Entity nmsEntity = ((CraftEntity) event.getEntity()).getHandle();
		if (nmsEntity instanceof MeteoriteComponent) {
			event.setCancelled(true);
			explode(event.getBlock().getLocation(), event.getEntity(),
					false, ((MeteoriteComponent) nmsEntity).shouldExplode());
			event.getEntity().remove();
			event.getBlock().setType(Material.AIR);
		}
	}

	/**
	 * Cause an explosion at a Location.
	 *
	 * @param loc the Location to explode at
	 */
	private void explode(Location loc, Entity explodeAs, boolean setFires, boolean terrainDamage) {
		Explosion explosion = new Explosion(((CraftWorld) loc.getWorld()).getHandle(),
				((CraftEntity) explodeAs).getHandle(), loc.getX(), loc.getY(), loc.getZ(), 4F, setFires, terrainDamage);
		explosion.a();
		explosion.a(true);
		loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
	}

	@Override
	public boolean isRequired() {
		// Due to poor design, the Meteors module is fragmented to a point where it's not feasible to disable.
		return true;
	}

	@Override
	public String getName() {
		return "Meteors";
	}

}
