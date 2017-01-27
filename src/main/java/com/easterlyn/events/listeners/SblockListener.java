package com.easterlyn.events.listeners;

import com.easterlyn.Easterlyn;

import org.bukkit.event.Listener;

/**
 * Abstraction for all Listeners to make dependency injection easier.
 * 
 * @author Jikoo
 */
public abstract class SblockListener implements Listener {

	private final Easterlyn plugin;

	public SblockListener(Easterlyn plugin) {
		this.plugin = plugin;
	}

	public Easterlyn getPlugin() {
		return plugin;
	}

}
