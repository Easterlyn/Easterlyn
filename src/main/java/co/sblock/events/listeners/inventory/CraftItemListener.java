package co.sblock.events.listeners.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.captcha.CruxiteDowel;
import co.sblock.chat.Language;
import co.sblock.events.listeners.SblockListener;
import co.sblock.utilities.InventoryUtils;

/**
 * Listener for CraftItemEvents.
 * 
 * @author Jikoo
 */
public class CraftItemListener extends SblockListener {

	public CraftItemListener(Sblock plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for CraftItemEvents.
	 * 
	 * @param event the CraftItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onCraftItem(final CraftItemEvent event) {
		for (ItemStack is : event.getInventory().getMatrix()) {
			if (is == null) {
				continue;
			}
			Player clicked = (Player) event.getWhoClicked();
			if (Captcha.isCard(is) || CruxiteDowel.isDowel(is)) {
				event.setCancelled(true);
				clicked.sendMessage(Language.getColor("bad") + "Using captchas in crafting tends to lose valuables!");
				clicked.updateInventory();
				return;
			}
			for (ItemStack is1 : InventoryUtils.getUniqueItems(getPlugin())) {
				if (is1.isSimilar(is)) {
					event.setCancelled(true);
					if (is.getItemMeta().hasDisplayName()) {
						clicked.sendMessage(Language.getColor("bad") + "You can't use a "
								+ is.getItemMeta().getDisplayName() + Language.getColor("bad") + " for that!");
					}
					return;
				}
			}
		}

		if (event.getClick().isShiftClick()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					((Player) event.getWhoClicked()).updateInventory();
				}
			}.runTask(getPlugin());
		}
	}
}
