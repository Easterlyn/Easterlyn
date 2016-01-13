package co.sblock.events.listeners.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ExpBottleEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for ExpBottleEvents.
 * <p>
 * Since Sblock allows bottling experience for 11 exp and an empty bottle, a fixed return is far
 * kinder than vanilla's 3-11.
 * 
 * @author Jikoo
 */
public class ExpBottleListener extends SblockListener {

	public ExpBottleListener(Sblock plugin) {
		super(plugin);
	}

	@EventHandler
	public void onExpBottle(ExpBottleEvent event) {
		event.setExperience(10);
	}
}
