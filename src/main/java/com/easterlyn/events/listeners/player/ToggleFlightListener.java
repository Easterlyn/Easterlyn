package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.Effects;
import com.easterlyn.events.listeners.SblockListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;

/**
 * Listener for PlayerToggleFlightEvents.
 * 
 * @author Jikoo
 */
public class ToggleFlightListener extends SblockListener {

	private final Effects effects;

	public ToggleFlightListener(Easterlyn plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
	}

	/**
	 * EventHandler for PlayerToggleFlightEvents.
	 * 
	 * @param event the PlayerToggleFlightEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		// Apply reactive Effects
		effects.handleEvent(event, event.getPlayer(), true);
	}

}
