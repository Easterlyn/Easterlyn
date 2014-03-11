package co.sblock.Sblock.Events.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Spectator.Spectators;

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
			if (Spectators.getSpectators().isSpectator(event.getPlayer().getName())) {
				event.setCancelled(true);
				p.sendMessage(ChatColor.RED + "You cannot visit another world in spectator mode!");
			}
			if (SblockEvents.getEvents().teleports.remove(p.getName())) {
				SblockUser u = SblockUser.getUser(p.getName());
				if (!u.isGodTier()) {
					u.setPreviousLocation(event.getFrom());
					u.updateFlight();
				}
			}
		}
	}
}
