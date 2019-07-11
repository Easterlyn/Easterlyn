package com.easterlyn.kitchensink.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class NoCreativeCrammingDrops implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			return;
		}

		if (event.getEntity().getLastDamageCause() != null
				&& event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.CRAMMING
				|| event.getEntity().getKiller() != null
				&& event.getEntity().getKiller().getGameMode() == GameMode.CREATIVE) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

}
