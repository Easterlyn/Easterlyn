package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import co.sblock.effects.Effects;

/**
 * Listener for PlayerToggleFlightEvents.
 * 
 * @author Jikoo
 */
public class ToggleFlightListener implements Listener {

	/**
	 * EventHandler for PlayerToggleFlightEvents.
	 * 
	 * @param event the PlayerToggleFlightEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		// Apply reactive Effects
		Effects.getInstance().handleEvent(event, event.getPlayer(), true);
	}
}
