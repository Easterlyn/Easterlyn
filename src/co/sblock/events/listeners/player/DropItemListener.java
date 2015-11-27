package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.type.computer.EmailWriter;
import co.sblock.machines.type.computer.Programs;
import co.sblock.users.User;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for PlayerDropItemEvents.
 * 
 * @author Jikoo
 */
public class DropItemListener extends SblockListener {

	private final Users users;
	private final EmailWriter email;

	public DropItemListener(Sblock plugin) {
		super(plugin);
		this.users = plugin.getModule(Users.class);
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
		User user = users.getUser(event.getPlayer().getUniqueId());
		if (user == null) {
			return;
		}

		if (user.isServer()) {
			event.setCancelled(true);
			return;
		}
	}
}
