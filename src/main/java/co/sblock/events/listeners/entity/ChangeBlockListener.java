package co.sblock.events.listeners.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.Meteors;

/**
 * Listener for EntityChangeBlockEvents.
 * 
 * @author Jikoo
 */
public class ChangeBlockListener extends SblockListener {

	private final Meteors meteors;

	public ChangeBlockListener(Sblock plugin) {
		super(plugin);
		this.meteors = plugin.getModule(Meteors.class);
	}

	/**
	 * EventHandler for EntityChangeBlockEvents to handle Meteorite FallingBlock landings.
	 * 
	 * @param event the EntityChangeBlockEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.getEntityType() != EntityType.FALLING_BLOCK) {
			return;
		}
		meteors.handlePotentialMeteorite(event);
	}
}
