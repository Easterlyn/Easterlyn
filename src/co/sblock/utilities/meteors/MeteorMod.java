package co.sblock.utilities.meteors;

import net.minecraft.server.v1_7_R4.Explosion;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import co.sblock.module.Module;

/**
 * @author Dublek, Jikoo
 */
public class MeteorMod extends Module implements Listener {
	/** The MeteorMod instance. */
	private static MeteorMod instance;

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
	public static MeteorMod getInstance() {
		return instance;
	}

	/**
	 * Deals with EntityChangeBlockEvents to keep all related NMS/OBC access in one area.
	 * 
	 * @param event the EntityChangeBlockEvent
	 */
	public void handlePotentialMeteorite(EntityChangeBlockEvent event) {
		net.minecraft.server.v1_7_R4.Entity nmsEntity = ((CraftEntity) event.getEntity()).getHandle();
		if (nmsEntity instanceof MeteoriteComponent) {
			event.setCancelled(true);
			MeteorMod.getInstance().explode(event.getBlock().getLocation(), event.getEntity(),
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
				((CraftEntity) explodeAs).getHandle(), loc.getX(), loc.getY(), loc.getZ(), 4F);
		explosion.a = setFires;
		explosion.b = terrainDamage;
		explosion.a();
		explosion.a(true);
		loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 4);
	}

	@Override
	protected String getModuleName() {
		return "Meteors";
	}
}
