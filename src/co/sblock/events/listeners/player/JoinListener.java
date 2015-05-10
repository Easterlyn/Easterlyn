package co.sblock.events.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.effects.FXManager;
import co.sblock.events.Events;
import co.sblock.users.OnlineUser;
import co.sblock.users.Region;
import co.sblock.users.Users;
import co.sblock.utilities.messages.SlackMessenger;

/**
 * Listener for PlayerJoinEvents.
 * 
 * @author Jikoo
 */
public class JoinListener implements Listener {

	/**
	 * The event handler for PlayerJoinEvents.
	 * 
	 * @param event the PlayerJoinEvent
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		event.setJoinMessage(null);
		// CHAT: check message beforehand and don't announce channels if muted
		Users.getGuaranteedUser(event.getPlayer().getUniqueId());

		Events.getInstance().addCachedIP(event.getPlayer().getAddress().getHostString(), event.getPlayer().getName());

		SlackMessenger.post(event.getPlayer().getName(), event.getPlayer().getUniqueId(), event.getPlayer().getName() + " logs in.");

		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Users.team(player);
				OnlineUser user = Users.getGuaranteedUser(player.getUniqueId()).getOnlineUser();
				user.handleLoginChannelJoins();
				Region region = user.getCurrentRegion();
				user.updateCurrentRegion(region);
				// On login, conditions for setting rpack are not met, must be done here
				player.setResourcePack(region.getResourcePackURL());
				user.updateFlight();
				FXManager.getInstance().getInvisibilityManager().updateVisibility(player);
				for (String command : user.getLoginCommands()) {
					player.chat(command);
				}
			}
		}.runTask(Sblock.getInstance());
	}
}
