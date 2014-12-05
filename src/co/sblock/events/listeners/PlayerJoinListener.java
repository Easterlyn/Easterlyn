package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.events.Events;
import co.sblock.users.Users;

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
		Users.getGuaranteedUser(event.getPlayer().getUniqueId());

		Events.getInstance().addCachedIP(event.getPlayer().getAddress().getHostString(), event.getPlayer().getName());

		new BukkitRunnable() {
			@Override
			public void run() {
				Users.team(event.getPlayer());
			}
		}.runTask(Sblock.getInstance());
	}
}
