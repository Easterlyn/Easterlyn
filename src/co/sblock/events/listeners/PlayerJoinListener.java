package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.users.UserManager;

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
	public void onPlayerJoin(final PlayerJoinEvent event) {
		event.setJoinMessage(null);
		// TODO check message beforehand and don't announce channels if muted
		UserManager.loadUser(event.getPlayer().getUniqueId());

		new BukkitRunnable() {

			@Override
			public void run() {
				UserManager.team(event.getPlayer());
			}
		}.runTask(Sblock.getInstance());
	}
}
