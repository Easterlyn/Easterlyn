package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import co.sblock.machines.type.computer.EmailWriter;
import co.sblock.machines.type.computer.Programs;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for PlayerDropItemEvents.
 * 
 * @author Jikoo
 */
public class DropItemListener implements Listener {

	private final EmailWriter email;

	public DropItemListener() {
		this.email = (EmailWriter) Programs.getProgramByName("EmailWriter");
	}

	/**
	 * EventHandler for PlayerDropItemEvents.
	 * 
	 * @param event the PlayerDropItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent event) {
		if (email.isLetter(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
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
		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user == null) {
			return;
		}

		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
