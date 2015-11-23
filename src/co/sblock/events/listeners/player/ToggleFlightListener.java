package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for PlayerToggleFlightEvents.
 * 
 * @author Jikoo
 */
public class ToggleFlightListener extends SblockListener {

	private final Effects effects;

	public ToggleFlightListener(Sblock plugin) {
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
