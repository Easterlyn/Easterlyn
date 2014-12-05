package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.util.StringUtil;

import co.sblock.users.UserManager;

/**
 * Listener for PlayerChatTabCompleteEvents.
 * 
 * @author Jikoo
 */
public class PlayerChatTabCompleteListener implements Listener {

	@EventHandler
	public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent event) {
		if (event.getChatMessage().split(" ")[0].equals(event.getLastToken())
				&& event.getLastToken().length() > 0 && event.getLastToken().charAt(0) == '@') {
			String completing = event.getLastToken().substring(1);
			for (String channel : UserManager.getGuaranteedUser(event.getPlayer().getUniqueId()).getListening()) {
				if (StringUtil.startsWithIgnoreCase(channel, completing)) {
					event.getTabCompletions().add("@" + channel);
				}
			}
		}
	}
}
