package co.sblock.events.listeners.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;

/**
 * Listener for ChunkLoadEvents.
 * 
 * @author Jikoo
 */
public class ChunkLoadListener extends SblockListener {

	private final Machines machines;

	public ChunkLoadListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		machines.loadChunkMachines(event.getChunk());
	}

}
