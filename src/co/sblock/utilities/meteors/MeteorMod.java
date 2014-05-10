package co.sblock.utilities.meteors;

import java.util.HashSet;
import java.util.Iterator;

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
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Sblock;
import co.sblock.events.packets.WrapperPlayServerWorldParticles;
import co.sblock.module.Module;

/**
 * @author Dublek, Jikoo
 */
public class MeteorMod extends Module implements Listener {
	/** The MeteorMod instance. */
	private static MeteorMod instance;

	/** The MeteorCommandListener. */
	private MeteorCommandListener meteorListener;
	/** The Set of Meteorite FallingBlock UUIDs. */
	private HashSet<Entity> entities;
	/** Task ID for creating particle effects on Meteorites. */
	private int task;

	private final WrapperPlayServerWorldParticles PACKET_LAVA;

	public MeteorMod() {
		this.PACKET_LAVA = new WrapperPlayServerWorldParticles();
		this.PACKET_LAVA.setParticleEffect(WrapperPlayServerWorldParticles.ParticleEffect.LAVA);
		this.PACKET_LAVA.setNumberOfParticles(1);
		this.PACKET_LAVA.setOffset(new Vector(0.5, 0.5, 0.5));
	}

	/**
	 * @see Module#onEnable()
	 */
	@Override
	public void onEnable() {
		instance = this;
		entities = new HashSet<>();
		this.meteorListener = new MeteorCommandListener();
		this.registerCommands(this.meteorListener);
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
	 * Add an Entity.
	 * 
	 * @param entity the Entity
	 */
	public void addEntity(Entity entity) {
		entities.add(entity);
		if (task == -1) {
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {
				@Override
				public void run() {
					Iterator<Entity> iterator = entities.iterator();
					while (iterator.hasNext()) {
						Entity entity = iterator.next();
						PACKET_LAVA.setLocation(entity.getLocation());
						ProtocolLibrary.getProtocolManager().broadcastServerPacket(PACKET_LAVA.getHandle(), entity.getLocation(), 48);
						if (entity.isDead()) {
							iterator.remove();
							continue;
						}
					}

					if (entities.size() == 0) {
						Bukkit.getScheduler().cancelTask(task);
						task = -1;
						return;
					}
				}
			}, 1, 1);
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
