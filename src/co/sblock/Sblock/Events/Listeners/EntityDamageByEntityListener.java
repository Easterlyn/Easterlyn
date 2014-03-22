package co.sblock.Sblock.Events.Listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import co.sblock.Sblock.SblockEffects.ActiveEffect;
import co.sblock.Sblock.SblockEffects.ActiveEffectType;
import co.sblock.Sblock.SblockEffects.EffectManager;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Spectator.Spectators;

/**
 * Listener for EntityDamageByEntityEvents.
 * 
 * @author Jikoo
 */
public class EntityDamageByEntityListener implements Listener {

	/**
	 * EventHandler for EntityDamageByEntityEvents.
	 * 
	 * @param event the EntityDamageByEntityEvent
	 */
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) {
			return;
		}

		Player p = (Player) event.getDamager();

		if (Spectators.getSpectators().isSpectator(p.getName())) {
			p.sendMessage(ChatColor.RED + "You waggle your fingers wildly, but your target remains unmussed.");
			event.setCancelled(true);
			return;
		}

		SblockUser u = SblockUser.getUser(p.getName());
		if (u != null && u.isServer()) {
			event.setCancelled(true);
			return;
		}

		if (event.getEntity() instanceof Player) {
			Player target = (Player) event.getEntity();
			HashMap<ActiveEffect, Integer> effects = EffectManager.activeScan(p);
			if (effects.isEmpty()) return;
			for (ActiveEffect aE : effects.keySet()) {
				if (aE.getActiveEffectType() == ActiveEffectType.DAMAGE) {
					ActiveEffect.applyDamageEffect(p, target, aE, effects.get(aE));
				}
			}
		}
	}
}
