package com.easterlyn.events.listeners.entity;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.SblockListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ExpBottleEvent;

/**
 * Listener for ExpBottleEvents.
 * <p>
 * Since Easterlyn allows bottling experience for 11 exp and an empty bottle, a fixed return is far
 * kinder than vanilla's 3-11.
 * 
 * @author Jikoo
 */
public class ExpBottleListener extends SblockListener {

	public ExpBottleListener(Easterlyn plugin) {
		super(plugin);
	}

	@EventHandler
	public void onExpBottle(ExpBottleEvent event) {
		event.setExperience(10);
	}

}
