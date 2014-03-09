package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import co.sblock.Sblock.Utilities.Spectator.Spectators;

/**
 * Listener for PlayerPickupItemEvents.
 * 
 * @author Jikoo
 */
public class PlayerPickupItemListener implements Listener {

	/**
	 * EventHandler for PlayerPickupItemEvents.
	 * 
	 * @param event the PlayerPickupItemEvent
	 */
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getName())) {
			event.setCancelled(true);
			return;
		}
	}
}
