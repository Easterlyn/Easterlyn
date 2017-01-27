package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.captcha.CruxiteDowel;
import com.easterlyn.chat.Language;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.PermissionUtils;

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
public class CraftItemListener extends SblockListener {

	private final Events events;
	private final Language lang;
	private final Machines machines;

	public CraftItemListener(Easterlyn plugin) {
		super(plugin);
		this.events = plugin.getModule(Events.class);
		this.lang = plugin.getModule(Language.class);
		this.machines = plugin.getModule(Machines.class);

		PermissionUtils.addParent("sblock.events.creative.unfiltered", "sblock.felt");
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
				&& !event.getWhoClicked().hasPermission("sblock.events.creative.unfiltered")
				&& events.getCreativeBlacklist().contains(event.getCurrentItem().getType())) {
			event.setCancelled(true);
			return;
		}

		for (ItemStack is : event.getInventory().getMatrix()) {
			if (is == null) {
				continue;
			}
			Player clicked = (Player) event.getWhoClicked();
			if (Captcha.isCard(is) || CruxiteDowel.isDowel(is)) {
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
