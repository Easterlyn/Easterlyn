package co.sblock.events.listeners.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;

/**
 * Listener for ExpBottleEvents.
 * <p>
 * Since Sblock allows bottling experience for 11 exp and an empty bottle, a fixed return is far
 * kinder than vanilla's 3-11.
 * 
 * @author Jikoo
 */
public class ExpBottleListener implements Listener {

	@EventHandler
	public void onExpBottle(ExpBottleEvent event) {
		event.setExperience(10);
	}
}
