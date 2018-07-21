package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.events.listeners.EasterlynListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

/**
 * Listener for PrepareItemEnchantEvents.
 *
 * @author Jikoo
 */
public class PrepareItemEnchantListener extends EasterlynListener {

	public PrepareItemEnchantListener(Easterlyn plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for PrepareItemEnchantEvents.
	 *
	 * @param event the PrepareItemEnchantEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
		if (Captcha.isCaptcha(event.getItem())) {
			event.setCancelled(true);
		}
	}

}
