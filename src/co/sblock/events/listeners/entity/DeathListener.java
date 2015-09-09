package co.sblock.events.listeners.entity;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Listener for EntityDeathEvents.
 * 
 * @author Jikoo
 */
public class DeathListener implements Listener {

	/**
	 * EventHandler for EntityDeathEvents.
	 * 
	 * @param event the EntityDeathEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			return;
		}
		if (event.getEntity().getKiller() != null
				&& event.getEntity().getKiller().getGameMode() == GameMode.CREATIVE) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

}
