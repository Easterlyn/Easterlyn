package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.inventory.InventoryUtils;

/**
 * Listener for CraftItemEvents.
 * 
 * @author Jikoo
 */
public class CraftItemListener implements Listener {

	/**
	 * EventHandler for CraftItemEvents.
	 * 
	 * @param event the CraftItemEvent
	 */
	@EventHandler
	public void onPrepareItemCraft(CraftItemEvent event) {
		for (ItemStack is : event.getInventory().getContents()) {
			if (is == null) {
				continue;
			}
			Player clicked = (Player) event.getWhoClicked();
			if (Captcha.isCard(is)) {
				event.setCancelled(true);
				clicked.sendMessage(ChatColor.RED + "Using captchas in crafting tends to lose valuables!");
				clicked.updateInventory();
				return;
			}
			for (ItemStack is1 : InventoryUtils.getUniqueItems()) {
				if (is1.isSimilar(is)) {
					event.setCancelled(true);
					if (is.getItemMeta().hasDisplayName()) {
						clicked.sendMessage(ChatColor.RED + "You can't use a "
								+ is.getItemMeta().getDisplayName() + ChatColor.RED + " for that!");
					}
				}
			}
		}
	}
}
