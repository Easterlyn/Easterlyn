package co.sblock.micromodules;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import co.sblock.Sblock;
import co.sblock.module.Module;

import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;

import net.minecraft.server.v1_9_R1.Explosion;

/**
 * A Module for creating falling explosive spheres.
 * 
 * @author Dublek, Jikoo
 */
public class Meteors extends Module {

	/* The Meteors instance. */
	private static Meteors instance;

	public Meteors(Sblock plugin) {
		super(plugin);
	}

	/**
	 * @see Module#onEnable()
	 */
	@Override
	public void onEnable() {
		instance = this;
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	public void onDisable() {
		instance = null;
	}

	/**
	 * Gets the MeteorMod instance.
	 * 
	 * @return the MeteorMod instance
	 */
	public static Meteors getInstance() {
		return instance;
	}

	/**
	 * Deals with EntityChangeBlockEvents to keep all related NMS/OBC access in one area.
	 * 
	 * @param event the EntityChangeBlockEvent
	 */
	public void handlePotentialMeteorite(EntityChangeBlockEvent event) {
		net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) event.getEntity()).getHandle();
		if (nmsEntity instanceof MeteoriteComponent) {
			event.setCancelled(true);
			Meteors.getInstance().explode(event.getBlock().getLocation(), event.getEntity(),
					false, ((MeteoriteComponent) nmsEntity).shouldExplode());
			event.getEntity().remove();
			event.getBlock().setType(Material.AIR);
			return;
		}
	}

	/**
	 * Cause an explosion at a Location.
	 * 
	 * @param loc the Location to explode at
	 */
	public void explode(Location loc, Entity explodeAs, boolean setFires, boolean terrainDamage) {
		Explosion explosion = new Explosion(((CraftWorld) loc.getWorld()).getHandle(),
				((CraftEntity) explodeAs).getHandle(), loc.getX(), loc.getY(), loc.getZ(), 4F, setFires, terrainDamage);
		explosion.a();
		explosion.a(true);
		loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 4);
	}

	@Override
	public String getName() {
		return "Meteors";
	}

}
