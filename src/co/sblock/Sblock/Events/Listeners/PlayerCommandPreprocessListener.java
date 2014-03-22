package co.sblock.Sblock.Events.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import co.sblock.Sblock.Utilities.Spectator.Spectators;

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
		if (event.getMessage().length() > 4 && event.getMessage().substring(1, 5).equalsIgnoreCase("vote")) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Voting is now done by sleeping! Hold shift and right click your bed.");
		}
		if (event.getMessage().length() > 7 && event.getMessage().substring(1, 8).equalsIgnoreCase("sethome")
				&& Spectators.getSpectators().isSpectator(event.getPlayer().getName())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You hear a fizzling noise as your spell fails.");
		}
	}
}
