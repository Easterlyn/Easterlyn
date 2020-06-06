package com.easterlyn.kitchensink.listener;

import com.easterlyn.event.ReportableEvent;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class KillerRabbit implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onEntitySpawn(CreatureSpawnEvent event) {
		if (event.getEntityType() != EntityType.RABBIT
				|| event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
				|| ThreadLocalRandom.current().nextInt(1000) > 1) {
			return;
		}

		Rabbit rabbit = (Rabbit) event.getEntity();
		rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
		rabbit.setCustomName("The Killer Bunny");
		rabbit.setCustomNameVisible(true);
		rabbit.setRemoveWhenFarAway(false);

		ReportableEvent.call(String.format("Spawned the Killer of Caerbannog at %s, %s, %s",
				rabbit.getLocation().getBlockX(), rabbit.getLocation().getBlockY(), rabbit.getLocation().getBlockZ()));
	}

}
