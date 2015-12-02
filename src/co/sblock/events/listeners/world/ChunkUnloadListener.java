package co.sblock.events.listeners.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;

/**
 * Listener for ChunkUnloadEvents.
 * 
 * @author Jikoo
 */
public class ChunkUnloadListener extends SblockListener {

	private final Machines machines;

	public ChunkUnloadListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		machines.unloadChunkMachines(event.getChunk());
	}

}
