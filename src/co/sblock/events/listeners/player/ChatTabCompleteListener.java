package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.util.StringUtil;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.users.Users;

/**
 * Listener for PlayerChatTabCompleteEvents.
 * 
 * @author Jikoo
 */
public class ChatTabCompleteListener extends SblockListener {

	private final Users users;

	public ChatTabCompleteListener(Sblock plugin) {
		super(plugin);
		this.users = plugin.getModule(Users.class);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent event) {
		if (!event.getChatMessage().equals(event.getLastToken())) {
			// Not tab completing first section, ignore
			return;
		}
		if (event.getChatMessage().isEmpty()) {
			for (String channel : users.getUser(event.getPlayer().getUniqueId()).getListening()) {
				event.getTabCompletions().add("@" + channel);
			}
		} else if (event.getLastToken().charAt(0) == '@') {
			String completing = event.getLastToken().substring(1);
			for (String channel : users.getUser(event.getPlayer().getUniqueId()).getListening()) {
				if (StringUtil.startsWithIgnoreCase(channel, completing)) {
					event.getTabCompletions().add("@" + channel);
				}
			}
		}
	}
}
