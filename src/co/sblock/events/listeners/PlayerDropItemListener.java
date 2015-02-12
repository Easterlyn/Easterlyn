package co.sblock.events.listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import co.sblock.effects.FXManager;
import co.sblock.effects.fx.SblockFX;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for PlayerDropItemEvents.
 * 
 * @author Jikoo
 */
public class PlayerDropItemListener implements Listener {

	/**
	 * EventHandler for PlayerDropItemEvents.
	 * 
	 * @param event the PlayerDropItemEvent
	 */
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		// Cruxite items should not be tradeable.
		if (event.getItemDrop().getItemStack().getItemMeta().hasDisplayName()
				&& event.getItemDrop().getItemStack().getItemMeta().getDisplayName()
						.startsWith(ChatColor.AQUA + "Cruxite ")) {
			event.setCancelled(true);
			return;
		}

		// valid SblockUser required for all events below this point
		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user == null) {
			return;
		}

		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			event.setCancelled(true);
			return;
		}

		HashMap<String, SblockFX> effects = FXManager.getInstance().itemScan(event.getItemDrop().getItemStack());
		for (SblockFX fx : effects.values()) {
			user.getOnlineUser().reduceEffect(fx, 1);
		}
	}
}
