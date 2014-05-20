package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.data.SblockData;
import co.sblock.users.User;

/**
 * Listener for PlayerAsyncChatEvents.
 * 
 * @author Jikoo
 */
public class PlayerAsyncChatListener implements Listener {

	/**
	 * The event handler for AsyncPlayerChatEvents.
	 * 
	 * @param event the AsyncPlayerChatEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		// Clear recipients so as to not duplicate messages for global chat
		event.getRecipients().clear();
		User user = User.getUser(event.getPlayer().getUniqueId());
		if (user != null) {
			user.chat(event.getMessage(), false);
			// Uncancel global chat to play nice with IRC plugins/Dynmap
			if (user.isSuppressing() || user.isMute()) {
				// Do not uncancel, user cannot chat in current state.
			} else if (event.getMessage().startsWith("@# ") && event.getMessage().length() > 3) {
				event.setMessage(event.getMessage().substring(3));
				event.setCancelled(false);
			} else if (user.getCurrent().getName().equals("#")
					&& !(event.getMessage().startsWith("@") && event.getMessage().indexOf(' ') > 1)) {
				event.setCancelled(false);
			}
		} else {
			event.getPlayer().sendMessage(ChatColor.BOLD
					+ "[o] Your Sblock playerdata appears to not be loaded."
					+ "\nI'll take care of that for you, but make sure your /profile is correct.");
			SblockData.getDB().loadUserData(event.getPlayer().getUniqueId());
		}
	}
}
