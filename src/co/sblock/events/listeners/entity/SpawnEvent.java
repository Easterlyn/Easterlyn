package co.sblock.events.listeners.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * Listener for CreatureSpawnEvents.
 * 
 * @author Jikoo
 */
public class SpawnEvent implements Listener {

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
		if (Math.random() * 2500 > 1) {
			return;
		}
		Rabbit rabbit = (Rabbit) event.getEntity();
		rabbit.setRabbitType(Type.THE_KILLER_BUNNY);
		rabbit.setCustomName("The Killer Bunny");
		rabbit.setCustomNameVisible(true);
	}
}
