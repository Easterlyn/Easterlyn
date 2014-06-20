package co.sblock.utilities.meteors;

import net.minecraft.server.v1_7_R3.Explosion;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import co.sblock.module.Module;

/**
 * @author Dublek, Jikoo
 */
public class MeteorMod extends Module implements Listener {
	/** The MeteorMod instance. */
	private static MeteorMod instance;

	/** The MeteorCommandListener. */
	private MeteorCommandListener meteorListener;

	/**
	 * @see Module#onEnable()
	 */
	@Override
	public void onEnable() {
		instance = this;
		this.meteorListener = new MeteorCommandListener();
		this.registerCommands(this.meteorListener);
		this.registerEvents(this);
		// startReckoning(20*20);
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	public void onDisable() {
		// stopReckoning();
	}

	/**
	 * Gets the MeteorCommandListener. For creating meteors or crotchrockets easily.
	 * 
	 * @return the MeteorCommandListener
	 */
	public MeteorCommandListener getCommandListener() {
		return this.meteorListener;
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
	 * The EventHandler for EntityChangeBlockEvents to handle Meteorite FallingBlock landings.
	 * 
	 * @param event the EntityChangeBlockEvent
	 */
	@EventHandler
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.getEntityType() != EntityType.FALLING_BLOCK) {
			return;
		}
		if (((org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity) event.getEntity()).getHandle() instanceof MeteoriteComponent) {
			event.setCancelled(true);
			explode(event.getBlock().getLocation(), event.getEntity());
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
	public void explode(Location loc, Entity explodeAs) {
		Explosion explosion = new Explosion(((CraftWorld) loc.getWorld()).getHandle(),
				((CraftEntity) explodeAs).getHandle(), loc.getX(), loc.getY(), loc.getZ(), 4F);
		// Explosion.a = doFireDamage, set fire to terrain
		explosion.a = false;
		explosion.b = ((MeteoriteComponent) ((CraftEntity) explodeAs).getHandle()).shouldExplode();
		explosion.a();
		explosion.a(true);
		loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 4);
	}

	@Override
	protected String getModuleName() {
		return "Meteors";
	}
}
