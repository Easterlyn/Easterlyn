package co.sblock.events.listeners.entity;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for CreatureSpawnEvents.
 * 
 * @author Jikoo
 */
public class SpawnListener extends SblockListener {

	public SpawnListener(Sblock plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for CreatureSpawnEvents.
	 * 
	 * @param event the CreatureSpawnEvent
	 */
	public void onEntitySpawn(CreatureSpawnEvent event) {
		if (event.getEntityType() != EntityType.RABBIT) {
			return;
		}
		if (event.getSpawnReason() != SpawnReason.CHUNK_GEN && event.getSpawnReason() != SpawnReason.NATURAL) {
			return;
		}
		if (ThreadLocalRandom.current().nextInt(1000) > 1) {
			return;
		}
		Rabbit rabbit = (Rabbit) event.getEntity();
		rabbit.setRabbitType(Type.THE_KILLER_BUNNY);
		rabbit.setCustomName("The Killer Bunny");
		rabbit.setCustomNameVisible(true);
		rabbit.setRemoveWhenFarAway(false);
	}
}
