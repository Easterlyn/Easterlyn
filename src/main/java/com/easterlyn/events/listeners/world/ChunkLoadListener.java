package com.easterlyn.events.listeners.world;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;

import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * Listener for ChunkLoadEvents.
 * 
 * @author Jikoo
 */
public class ChunkLoadListener extends EasterlynListener {

	private final Machines machines;

	public ChunkLoadListener(Easterlyn plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (event.isNewChunk()) {
			machines.deleteChunkMachines(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
		}
		machines.enableChunkMachines(event.getChunk());
	}

}
