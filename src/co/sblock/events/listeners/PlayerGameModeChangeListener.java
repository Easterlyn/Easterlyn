package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for PlayerGameModeChangeEvents.
 * 
 * @author Jikoo
 */
public class PlayerGameModeChangeListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (Spectators.getInstance().isSpectator(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}
}
