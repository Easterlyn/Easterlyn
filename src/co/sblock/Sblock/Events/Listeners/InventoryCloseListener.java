package co.sblock.Sblock.Events.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import co.sblock.Sblock.SblockEffects.EffectManager;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Captcha.Captchadex;
import co.sblock.Sblock.Utilities.Inventory.InventoryManager;

/**
 * Listener for InventoryCloseEvents.
 * 
 * @author Jikoo
 */
public class InventoryCloseListener implements Listener {

	/**
	 * EventHandler for InventoryCloseEvents.
	 * 
	 * @param event the InventoryCloseEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getName().equals("Captchadex")) {
			Captchadex.saveCaptchadex(event.getInventory(), event.getPlayer().getItemInHand());
			// Adam verify hotbar slot isn't switched - latency etc.
		}

		InventoryManager.restoreInventory((Player) event.getPlayer());

		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		u.setAllPassiveEffects(EffectManager.passiveScan((Player) event.getPlayer()));
		EffectManager.applyPassiveEffects(u);
	}
}
