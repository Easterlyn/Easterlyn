package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.util.StringUtil;

import co.sblock.users.Users;

/**
 * Listener for PlayerChatTabCompleteEvents.
 * 
 * @author Jikoo
 */
public class ChatTabCompleteListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent event) {
		if (!event.getChatMessage().equals(event.getLastToken())) {
			// Not tab completing first section, ignore
			return;
		}
		if (event.getChatMessage().isEmpty()) {
			for (String channel : Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getListening()) {
				event.getTabCompletions().add("@" + channel);
			}
		} else if (event.getLastToken().charAt(0) == '@') {
			for (String channel : Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getListening()) {
				String completing = event.getLastToken().substring(1);
				if (StringUtil.startsWithIgnoreCase(channel, completing)) {
					event.getTabCompletions().add("@" + channel);
				}
			}
		}
	}
}
