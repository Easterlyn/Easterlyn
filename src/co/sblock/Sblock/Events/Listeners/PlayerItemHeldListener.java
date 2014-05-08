package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import co.sblock.Sblock.Events.SblockEvents;

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
