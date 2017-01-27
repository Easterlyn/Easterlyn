package com.easterlyn.events.listeners.world;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.micromodules.Holograms;

import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Listener for ChunkUnloadEvents.
 * 
 * @author Jikoo
 */
public class ChunkUnloadListener extends SblockListener {

	private final Holograms holograms;
	private final Machines machines;

	public ChunkUnloadListener(Easterlyn plugin) {
		super(plugin);
		this.holograms = plugin.getModule(Holograms.class);
		this.machines = plugin.getModule(Machines.class);
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		holograms.removeHolograms(event.getChunk());
		machines.disableChunkMachines(event.getChunk());
	}

}
