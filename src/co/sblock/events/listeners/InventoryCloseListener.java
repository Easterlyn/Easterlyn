package co.sblock.events.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import co.sblock.effects.EffectManager;
import co.sblock.machines.MachineInventoryTracker;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.captcha.Captchadex;

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
		}

		MachineInventoryTracker.getTracker().closeMachine(event);

		User user = UserManager.getUser(event.getPlayer().getUniqueId());
		if (user == null) {
			return; // Player is probably logging out
		}
		user.setAllPassiveEffects(EffectManager.passiveScan((Player) event.getPlayer()));
		EffectManager.applyPassiveEffects(user);
	}
}
