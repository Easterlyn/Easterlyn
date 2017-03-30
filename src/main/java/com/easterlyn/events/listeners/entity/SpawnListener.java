package com.easterlyn.events.listeners.entity;

import java.util.concurrent.ThreadLocalRandom;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.VillagerAdjustment;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Merchant;

/**
 * Listener for CreatureSpawnEvents.
 * 
 * @author Jikoo
 */
public class SpawnListener extends EasterlynListener {

	private final VillagerAdjustment villagers;

	public SpawnListener(Easterlyn plugin) {
		super(plugin);
		this.villagers = plugin.getModule(VillagerAdjustment.class);
	}

	/**
	 * EventHandler for CreatureSpawnEvents.
	 * 
	 * @param event the CreatureSpawnEvent
	 */
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		if (event.getEntity() instanceof Merchant) {
			villagers.adjustMerchant((Merchant) event.getEntity());
		}

		if (event.getEntityType() != EntityType.RABBIT
				|| event.getSpawnReason() != SpawnReason.CHUNK_GEN
						&& event.getSpawnReason() != SpawnReason.NATURAL
				|| ThreadLocalRandom.current().nextInt(1000) > 1) {
			return;
		}

		Rabbit rabbit = (Rabbit) event.getEntity();
		rabbit.setRabbitType(Type.THE_KILLER_BUNNY);
		rabbit.setCustomName("The Killer Bunny");
		rabbit.setCustomNameVisible(true);
		rabbit.setRemoveWhenFarAway(false);
	}

}
