package co.sblock.Sblock.Events.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.UserData.SblockUser;

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
		if (!event.getFrom().getWorld().equals(event.getTo().getWorld())
				&& SblockEvents.getEvents().teleports.remove(p.getName())) {
			SblockUser u = SblockUser.getUser(p.getName());
			if (!u.isGodTier()) {
				u.setPreviousLocation(event.getFrom());
				u.updateSleepstate();
			}
		}
	}
}
