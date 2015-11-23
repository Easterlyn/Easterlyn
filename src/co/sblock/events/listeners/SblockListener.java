package co.sblock.events.listeners;

import org.bukkit.event.Listener;

import co.sblock.Sblock;

/**
 * Abstraction for all Listeners to make dependency injection easier.
 * 
 * @author Jikoo
 */
public abstract class SblockListener implements Listener {

	private final Sblock plugin;

	public SblockListener(Sblock plugin) {
		this.plugin = plugin;
	}

	public Sblock getPlugin() {
		return plugin;
	}

}
