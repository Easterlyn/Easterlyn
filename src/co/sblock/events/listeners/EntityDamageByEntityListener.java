package co.sblock.events.listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import co.sblock.effects.ActiveEffect;
import co.sblock.effects.ActiveEffectType;
import co.sblock.effects.EffectManager;
import co.sblock.users.User;
import co.sblock.utilities.spectator.Spectators;

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

		if (Spectators.getSpectators().isSpectator(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED + "You waggle your fingers wildly, but your target remains unmussed.");
			event.setCancelled(true);
			return;
		}

		User u = User.getUser(p.getUniqueId());
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
