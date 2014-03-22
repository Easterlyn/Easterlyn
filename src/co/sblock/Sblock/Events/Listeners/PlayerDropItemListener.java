package co.sblock.Sblock.Events.Listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import co.sblock.Sblock.SblockEffects.EffectManager;
import co.sblock.Sblock.SblockEffects.PassiveEffect;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Spectator.Spectators;

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
		if (Spectators.getSpectators().isSpectator(event.getPlayer().getName())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Inventory? Spectral beings don't have those, don't be silly.");
			return;
		}

		// valid SblockUser required for all events below this point
		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		if (u == null) {
			return;
		}

		if (u.isServer()) {
			event.setCancelled(true);
			return;
		}

		HashMap<PassiveEffect, Integer> effects = EffectManager.itemScan(event.getItemDrop());
		for (PassiveEffect e : effects.keySet()) {
			u.removePassiveEffect(e, effects.get(e));
		}
	}
}
