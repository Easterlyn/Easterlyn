package co.sblock.events.listeners.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for PrepareItemEnchantEvents.
 * 
 * @author Jikoo
 */
public class PrepareItemEnchantListener extends SblockListener {

	public PrepareItemEnchantListener(Sblock plugin) {
		super(plugin);
	}

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
