package co.sblock.events.listeners;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import co.sblock.users.OfflineUser;
import co.sblock.fx.FXManager;
import co.sblock.fx.SblockFX;
import co.sblock.users.Users;

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
		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());

		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}

		HashMap<String, SblockFX> effects = FXManager.getInstance().itemScan(event.getItem().getItemStack());
		for (SblockFX fx : effects.values()) {
			fx.applyEffect(Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getOnlineUser(), event);
		}
	}
}
