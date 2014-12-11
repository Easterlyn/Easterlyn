package co.sblock.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import co.sblock.Sblock;
import co.sblock.users.OfflineUser;
import co.sblock.users.Region;
import co.sblock.users.Users;

/**
 * Listener for PlayerTeleportEvents.
 * 
 * @author Jikoo
 */
public class PlayerTeleportListener implements Listener {

	/**
	 * The event handler for PlayerTeleportEvents.
	 * <p>
	 * This method is for events that are guaranteed to be completed.
	 * 
	 * @param event the PlayerTeleportEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleportHasOccurred(final PlayerTeleportEvent event) {
		// People keep doing stupid stuff like /home while falling from spawn
		event.getPlayer().setFallDistance(0);

		if (event.getTo().getWorld().equals(event.getFrom().getWorld())) {
			return;
		}

		Bukkit.getScheduler().runTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
				// Update region
				Region target;
				if (event.getPlayer().getWorld().getName().equals("Derspit")) {
					target = getTargetDreamPlanet(user, event.getFrom().getWorld().getName());
				} else {
					target = Region.getRegion(event.getTo().getWorld().getName());
				}
				user.updateCurrentRegion(target);
			}
		});
	}

	private Region getTargetDreamPlanet(OfflineUser user, String from) {
		Region fromRegion = Region.getRegion(from);
		if (!fromRegion.isMedium()) {
			return user.getDreamPlanet();
		}
		// future flight to dream planets
		return user.getDreamPlanet();
	}
}
