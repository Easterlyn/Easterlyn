package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.Spectators;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

/**
 * Listener for PlayerGameModeChangeEvents.
 * 
 * @author Jikoo
 */
public class GameModeChangeListener extends EasterlynListener {

	private final Events events;
	private final Spectators spectators;

	public GameModeChangeListener(Easterlyn plugin) {
		super(plugin);
		this.events = plugin.getModule(Events.class);
		this.spectators = plugin.getModule(Spectators.class);
	}

	/**
	 * EventHandler for PlayerGameModeChangeEvents.
	 * 
	 * @param event the PlayerGameModeChangeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (spectators.isSpectator(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	/**
	 * Second EventHandler for PlayerGameModeChangeEvents.
	 * Used to update Player name tag visibility after a GameMode change.
	 * 
	 * @param event the PlayerGameModeChangeEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerGameModeChangeMonitor(PlayerGameModeChangeEvent event) {
		events.getInvisibilityManager().lazyVisibilityUpdate(event.getPlayer());
	}

}
