package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEditBookEvent;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for PlayerEditBookEvents.
 * 
 * @author Jikoo
 */
public class BookEditListener extends SblockListener {

	private final Chat chat;

	public BookEditListener(Sblock plugin) {
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
