package co.sblock.Sblock.Events.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.UserData.SblockUser;

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
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		if (SblockUser.getUser(event.getPlayer().getName()) != null) {
			if (event.getMessage().charAt(0) == '\u002F') {
				event.getPlayer().performCommand(event.getMessage().substring(1));
			} else {
				ChatUserManager.getUserManager().getUser(event.getPlayer().getName()).chat(event.getMessage(), false);
			}
		} else {
			event.getPlayer().sendMessage(ChatColor.BOLD
					+ "[o] Your Sblock playerdata appears to not be loaded."
					+ "\nI'll take care of that for you, but make sure your /profile is correct.");
			SblockData.getDB().loadUserData(event.getPlayer().getName());
		}
	}
}
