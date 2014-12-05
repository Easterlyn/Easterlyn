package co.sblock.events.listeners;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import co.sblock.users.OfflineUser;
import co.sblock.fx.FXManager;
import co.sblock.fx.SblockFX;
import co.sblock.users.Users;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for PlayerPickupItemEvents.
 * 
 * @author Jikoo
 */
public class PlayerPickupItemListener implements Listener {

	/**
	 * EventHandler for PlayerPickupItemEvents.
	 * 
	 * @param event the PlayerPickupItemEvent
	 */
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (Spectators.getInstance().isSpectator(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			return;
		}

		// valid SblockUser required for all events below this point
		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user == null) {
			return;
		}

		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}

		HashMap<String, SblockFX> effects = FXManager.getInstance().itemScan(event.getItem().getItemStack());
		for (SblockFX fx : effects.values()) {
			fx.applyEffect(Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getOnlineUser(), PlayerPickupItemEvent.class);
		}
	}
}
