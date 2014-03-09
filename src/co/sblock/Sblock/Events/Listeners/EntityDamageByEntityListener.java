package co.sblock.Sblock.Events.Listeners;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import co.sblock.Sblock.SblockEffects.ActiveEffect;
import co.sblock.Sblock.SblockEffects.ActiveEffectType;
import co.sblock.Sblock.SblockEffects.EffectManager;

public class EntityDamageByEntityListener implements Listener {
	
	/**
	 * The event handler for EntityDamageByEntityEvent.
	 * 
	 * @param event the EntityDamageByEntityEvent
	 */
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player)	{
			Player p = (Player) event.getDamager();
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
