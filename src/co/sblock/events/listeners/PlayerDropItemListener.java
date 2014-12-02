package co.sblock.events.listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import co.sblock.fx.FXManager;
import co.sblock.fx.SblockFX;
import co.sblock.users.OfflineUser;
import co.sblock.users.UserManager;
import co.sblock.utilities.spectator.Spectators;

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
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Inventory? Spectral beings don't have those, don't be silly.");
			return;
		}

		// Cruxite items should not be tradeable.
		if (event.getItemDrop().getItemStack().getItemMeta().hasDisplayName()
				&& event.getItemDrop().getItemStack().getItemMeta().getDisplayName()
						.startsWith(ChatColor.AQUA + "Cruxite ")) {
			event.setCancelled(true);
			return;
		}

		// valid SblockUser required for all events below this point
		OfflineUser user = UserManager.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user == null) {
			return;
		}

		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}

		HashMap<String, SblockFX> effects = FXManager.itemScan(event.getItemDrop().getItemStack());
		for (SblockFX fx : effects.values()) {
			user.getOnlineUser().reduceEffect(fx, 1);
		}
	}
}
