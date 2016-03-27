package co.sblock.events.listeners.plugin;

import org.bukkit.event.EventHandler;

import com.github.jikoo.regionerator.event.RegioneratorChunkDeleteEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.module.Dependency;

/**
 * Listener for RegioneratorChunkDeleteEvents.
 * 
 * @author Jikoo
 */
@Dependency("Regionerator")
public class RegioneratorChunkDeleteListener extends SblockListener {

	private final Machines machines;

	public RegioneratorChunkDeleteListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	@EventHandler
	public void onRegioneratorChunkDelete(RegioneratorChunkDeleteEvent event) {
		machines.deleteChunkMachines(event.getWorld(), event.getChunkX(), event.getChunkZ());
	}

}
