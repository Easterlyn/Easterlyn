package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.events.listeners.EasterlynListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEditBookEvent;

/**
 * Listener for PlayerEditBookEvents.
 * 
 * @author Jikoo
 */
public class BookEditListener extends EasterlynListener {

	private final Chat chat;

	public BookEditListener(Easterlyn plugin) {
		super(plugin);
		this.chat = plugin.getModule(Chat.class);
	}

	/**
	 * The EventHandler for PlayerEditBookEvents.
	 * 
	 * @param event the PlayerEditBookEvent.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBookedit(PlayerEditBookEvent event) {
		// Muted players can't write books.
		if (chat.testForMute(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
	}

}
