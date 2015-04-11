package co.sblock.events.listeners.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

import co.sblock.utilities.captcha.Captcha;

/**
 * Listener for PrepareItemEnchantEvents.
 * 
 * @author Jikoo
 */
public class PrepareItemEnchantListener implements Listener {

	/**
	 * EventHandler for PrepareItemEnchantEvents.
	 * 
	 * @param event the PrepareItemEnchantEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
		if (Captcha.isCard(event.getItem())) {
			event.setCancelled(true);
		}
	}
}
