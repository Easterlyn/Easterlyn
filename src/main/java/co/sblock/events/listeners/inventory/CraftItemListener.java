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

	private final Language lang;

	public CraftItemListener(Sblock plugin) {
		super(plugin);
		this.lang = this.getPlugin().getModule(Language.class);
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
				clicked.sendMessage(lang.getValue("events.craft.captcha"));
				clicked.updateInventory();
				return;
			}
			for (ItemStack is1 : InventoryUtils.getUniqueItems(getPlugin())) {
				if (is1.isSimilar(is)) {
					event.setCancelled(true);
					if (is.getItemMeta().hasDisplayName()) {
						clicked.sendMessage(lang.getValue("events.craft.unique")
								.replace("{PARAMETER}", is.getItemMeta().getDisplayName()));
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
