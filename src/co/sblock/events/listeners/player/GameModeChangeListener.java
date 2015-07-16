package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import co.sblock.events.Events;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for PlayerGameModeChangeEvents.
 * 
 * @author Jikoo
 */
public class GameModeChangeListener implements Listener {

	/**
	 * EventHandler for PlayerGameModeChangeEvents.
	 * 
	 * @param event the PlayerGameModeChangeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (Spectators.getInstance().isSpectator(event.getPlayer().getUniqueId())) {
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
		Events.getInstance().getInvisibilityManager().lazyVisibilityUpdate(event.getPlayer());
	}
}
