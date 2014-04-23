package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import co.sblock.events.SblockEvents;

/**
 * Listener for PlayerHeldItemEvents.
 * 
 * @author Jikoo
 */
public class PlayerItemHeldListener implements Listener {

	/**
	 * EventHandler for PlayerHeldItemEvents.
	 * 
	 * @param event the PlayerHeldItemEvent
	 */
	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent event) {

		// No swapping hotbar slots while opening Captchadex.
		if (SblockEvents.getEvents().openingCaptchadex.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}
}
