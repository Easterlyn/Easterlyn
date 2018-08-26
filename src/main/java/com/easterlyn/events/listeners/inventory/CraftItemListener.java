package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.chat.Language;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.player.PermissionUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for CraftItemEvents.
 *
 * @author Jikoo
 */
public class CraftItemListener extends EasterlynListener {

	private final Events events;
	private final Language lang;

	public CraftItemListener(Easterlyn plugin) {
		super(plugin);
		this.events = plugin.getModule(Events.class);
		this.lang = plugin.getModule(Language.class);

		PermissionUtils.addParent("easterlyn.events.creative.unfiltered", UserRank.MOD.getPermission());
	}


	/**
	 * EventHandler for CraftItemEvents on monitor priority.
	 *
	 * @param event the CraftItemEvent
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCraftItemMonitor(final CraftItemEvent event) {

		if (event.getClick().isShiftClick()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					((Player) event.getWhoClicked()).updateInventory();
				}
			}.runTask(getPlugin());
		}

		// TODO if Compounding Unionizer, set recipe
	}

	/**
	 * EventHandler for CraftItemEvents.
	 *
	 * @param event the CraftItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onCraftItem(final CraftItemEvent event) {
		if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE
				&& !event.getWhoClicked().hasPermission("easterlyn.events.creative.unfiltered")
				&& events.getCreativeBlacklist().contains(event.getCurrentItem().getType())) {
			event.setCancelled(true);
			return;
		}

		for (ItemStack is : event.getInventory().getMatrix()) {
			if (is == null) {
				continue;
			}
			Player clicked = (Player) event.getWhoClicked();
			if (Captcha.isCaptcha(is)) {
				event.setCancelled(true);
				clicked.sendMessage(lang.getValue("events.craft.captcha"));
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
	}

}
