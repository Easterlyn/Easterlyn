package co.sblock.events.packets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.events.packets.WrapperPlayServerWorldParticles.ParticleEffect;

import com.comphenix.protocol.ProtocolLibrary;

/**
 * Utility for spawning particles at an entity every tick.
 * 
 * @author Jikoo
 */
public class ParticleUtils {

	private static ParticleUtils instance;

	/** The Set of Entities and respective ParticleEffect to play. */
	private HashMap<Entity, ParticleEffect> entities;
	/** Task ID */
	private int task;

	private final WrapperPlayServerWorldParticles PACKET;

	private ParticleUtils() {
		entities = new HashMap<>();
		this.PACKET = new WrapperPlayServerWorldParticles();
		this.PACKET.setNumberOfParticles(1);
		this.PACKET.setOffset(new Vector(0.5, 0.5, 0.5));
	}

	/**
	 * Add an Entity.
	 * 
	 * @param entity the Entity
	 */
	public void addEntity(Entity entity, ParticleEffect effect) {
		entities.put(entity, effect);
		if (task == -1) {
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(),
					new Runnable() {
						@Override
						public void run() {
							Iterator<Entry<Entity, ParticleEffect>> iterator = entities.entrySet()
									.iterator();
							while (iterator.hasNext()) {
								Entry<Entity, ParticleEffect> entry = iterator.next();
								PACKET.setLocation(entry.getKey().getLocation());
								PACKET.setParticleEffect(entry.getValue());
								ProtocolLibrary.getProtocolManager().broadcastServerPacket(
										PACKET.getHandle(), entry.getKey().getLocation(), 48);
								if (entry.getKey().isDead()) {
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
					}, 0, 1);
		}
	}

	public void remove(Entity entity) {
		entities.remove(entity);
	}

	public static ParticleUtils getInstance() {
		if (instance == null) {
			instance = new ParticleUtils();
		}
		return instance;
	}
}
