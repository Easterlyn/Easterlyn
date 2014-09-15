package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import co.sblock.data.SblockData;

/**
 * Listener for PlayerJoinEvents.
 * 
 * @author Jikoo
 */
public class PlayerJoinListener implements Listener {

	/**
	 * The event handler for PlayerJoinEvents.
	 * 
	 * @param event the PlayerJoinEvent
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		SblockData.getDB().loadUserData(event.getPlayer().getUniqueId());
	}
}
