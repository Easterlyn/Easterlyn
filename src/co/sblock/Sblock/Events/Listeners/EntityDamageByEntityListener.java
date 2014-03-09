package co.sblock.Sblock.Events.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import co.sblock.Sblock.Utilities.Spectator.Spectators;

/**
 * @author Jikoo
 *
 */
public class EntityDamageByEntityListener implements Listener {

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) {
			return;
		}
		if (Spectators.getSpectators().isSpectator(((Player) event.getDamager()).getName())) {
			((Player) event.getDamager()).sendMessage(ChatColor.RED + "You waggle your fingers wildly, but your target remains unmussed.");
			event.setCancelled(true);
			return;
		}
	}
}
