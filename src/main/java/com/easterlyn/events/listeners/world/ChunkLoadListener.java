package com.easterlyn.events.listeners.world;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.micromodules.VillagerAdjustment;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Merchant;

/**
 * Listener for ChunkLoadEvents.
 * 
 * @author Jikoo
 */
public class ChunkLoadListener extends EasterlynListener {

	private final Machines machines;
	private final VillagerAdjustment villagers;

	public ChunkLoadListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
		this.villagers = plugin.getModule(VillagerAdjustment.class);
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (event.isNewChunk()) {
			machines.deleteChunkMachines(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
		}
		machines.enableChunkMachines(event.getChunk());

		for (Entity entity : event.getChunk().getEntities()) {
			if (entity instanceof Merchant) {
				villagers.adjustMerchant((Merchant) entity);
			}
		}
	}

}
