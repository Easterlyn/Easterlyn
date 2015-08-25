package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;

import co.sblock.chat.Chat;

/**
 * Listener for PlayerEditBookEvents.
 * 
 * @author Jikoo
 */
public class BookEditListener implements Listener {

	/**
	 * The EventHandler for PlayerEditBookEvents.
	 * 
	 * @param event the PlayerEditBookEvent.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBookedit(PlayerEditBookEvent event) {
		// Muted players can't write books.
		if (Chat.getChat().testForMute(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

}
