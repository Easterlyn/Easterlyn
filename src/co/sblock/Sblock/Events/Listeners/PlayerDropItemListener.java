/**
 * 
 */
package co.sblock.Sblock.Events.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import co.sblock.Sblock.Utilities.Spectator.Spectators;

/**
 * @author Jikoo
 *
 */
public class PlayerDropItemListener implements Listener {

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getName())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Inventory? Spectral beings don't have those, don't be silly.");
			return;
		}
	}
}
