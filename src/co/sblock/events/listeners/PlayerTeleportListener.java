package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import co.sblock.events.SblockEvents;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for PlayerTeleportEvents.
 * 
 * @author Jikoo
 */
public class PlayerTeleportListener implements Listener {

	/**
	 * The event handler for PlayerTeleportEvents.
	 * 
	 * @param event the PlayerTeleportEvent
	 */
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player p = event.getPlayer();
		if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
			if (Spectators.getSpectators().isSpectator(p.getUniqueId())) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You cannot visit another world in spectator mode!");
			}
			if (SblockEvents.getEvents().teleports.remove(p.getName())) {
				User user = UserManager.getUser(p.getUniqueId());
				user.setPreviousLocation(event.getFrom());
				user.updateFlight();
			}
		}
	}
}
