package co.sblock.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import co.sblock.Sblock;
import co.sblock.events.SblockEvents;
import co.sblock.users.Region;
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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleportHasOccurred(final PlayerTeleportEvent event) {
		if (!event.getTo().getWorld().equals(event.getFrom().getWorld())) {
			return;
		}
		Bukkit.getScheduler().runTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				User user = UserManager.getUser(event.getPlayer().getUniqueId());
				if (!user.isLoaded()) {
					return;
				}
				// Update region
				Region target;
				if (event.getPlayer().getWorld().getName().equals("Derspit")) {
					target = getTargetDreamPlanet(user, event.getFrom().getWorld().getName());
				} else {
					target = Region.uValueOf(event.getTo().getWorld().getName());
				}
				user.updateCurrentRegion(target);
			}
		});
	}

	private Region getTargetDreamPlanet(User user, String from) {
		Region fromRegion = Region.uValueOf(from);
		if (!fromRegion.isMedium()) {
			return user.getDreamPlanet();
		}
		// future flight to dream planets
		return user.getDreamPlanet();
	}
}
