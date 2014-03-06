package co.sblock.Sblock.Events.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import co.sblock.Sblock.Events.SblockEvents;

/**
 * Listener for PlayerBedLeaveEvents.
 * 
 * @author Jikoo
 */
public class PlayerBedLeaveListener implements Listener {

	/**
	 * EventHandler for PlayerBedLeaveEvents.
	 * 
	 * @param event the PlayerBedLeaveEvent
	 */
	@EventHandler
	public void onEvent(PlayerBedLeaveEvent event) {
		try {
			Bukkit.getScheduler().cancelTask(SblockEvents.getEvents()
					.tasks.remove(event.getPlayer().getName()));
		} catch (NullPointerException e) {
			// Player is not sleep teleporting.
		}
	}
}
