package co.sblock.utilities.meteors;

import java.util.HashSet;

import net.minecraft.server.v1_7_R3.Explosion;

import org.bukkit.Bukkit;
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

import co.sblock.Module;
import co.sblock.Sblock;

/**
 * @author Dublek, Jikoo
 */
public class MeteorMod extends Module implements Listener {
	/** The MeteorMod instance. */
	private static MeteorMod instance;
	/** The Set of Meteorite FallingBlock UUIDs. */
	private HashSet<Entity> entities;
	/** Task ID for creating particle effects on Meteorites. */
	private int task;

	/**
	 * @see Module#onEnable()
	 */
	@Override
	public void onEnable() {
		instance = this;
		entities = new HashSet<>();
		this.registerCommands(new MeteorCommandListener());
		this.registerEvents(this);
		task = -1;
		// startReckoning(20*20);
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	public void onDisable() {
		for (Entity e : entities.toArray(new Entity[0])) {
			entities.remove(e);
			e.remove();
		}
		// stopReckoning();
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
	 * Add an Entity. If FallingBlock, specify if the entity is to explode on contact.
	 * 
	 * @param entity the Entity
	 * @param damage true if the explosion is to do terrain damage
	 */
	public void addEntity(Entity entity) {
		entities.add(entity);
		if (task == -1) {
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (entities.size() == 0) {
						Bukkit.getScheduler().cancelTask(task);
						task = -1;
						return;
					}
					for (Entity entity : entities.toArray(new Entity[0])) {
						if (entity.isDead()) {
							entities.remove(entity);
						}
						entity.getWorld().playEffect(entity.getLocation(), Effect.MOBSPAWNER_FLAMES, 48);
					}
				}
			}, 0, 4);
		}
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
		if (entities.contains(event.getEntity())) {
			event.setCancelled(true);
			entities.remove(event.getEntity());
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
}
