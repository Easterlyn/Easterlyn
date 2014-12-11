package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import co.sblock.users.Users;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for PlayerCommandPreprocessEvents.
 * 
 * @author Jikoo
 */
public class PlayerCommandPreprocessListener implements Listener {

	/**
	 * EventHandler for PlayerCommandPreprocessEvents.
	 * 
	 * @param event the PlayerCommandPreprocessEvent
	 */
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		int colon = event.getMessage().indexOf(':');
		int space = event.getMessage().indexOf(' ');
		if (!event.getPlayer().hasPermission("group.denizen") && 0 < colon && (colon < space || space < 0)) {
			event.setMessage("/" + event.getMessage().substring(colon));
		}

		String lowercase = event.getMessage().toLowerCase();
		if (lowercase.startsWith("/sethome")
				&& (Spectators.getInstance().isSpectator(event.getPlayer().getUniqueId())
				|| Users.getGuaranteedUser(event.getPlayer().getUniqueId()).isServer())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You hear a fizzling noise as your spell fails.");
			return;
		}

		if ((lowercase.startsWith("/gc") || lowercase.startsWith("/lag") || lowercase.startsWith("/mem")
				|| lowercase.startsWith("/uptime") || lowercase.startsWith("/entities"))
				&& !event.getPlayer().hasPermission("group.helper")) {
			event.setMessage("/tps");
			return;
		}
	}
}
