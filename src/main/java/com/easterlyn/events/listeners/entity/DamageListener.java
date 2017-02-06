package com.easterlyn.events.listeners.entity;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.Effects;
import com.easterlyn.events.listeners.EasterlynListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Listener for EntityDamageEvents.
 * 
 * @author Jikoo
 */
public class DamageListener extends EasterlynListener {

	private final Effects effects;

	public DamageListener(Easterlyn plugin) {
		super(plugin);
		this.effects = plugin.getModule(Effects.class);
	}

	/**
	 * EventHandler for EntityDamageEvents.
	 * 
	 * @param event the EntityDamageEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		effects.handleEvent(event, (Player) event.getEntity(), true);
	}

}
