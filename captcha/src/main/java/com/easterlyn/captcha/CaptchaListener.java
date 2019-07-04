package com.easterlyn.captcha;

import com.easterlyn.EasterlynCaptcha;
import com.easterlyn.util.inventory.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CaptchaListener implements Listener {

	private final EasterlynCaptcha captcha;

	public CaptchaListener(EasterlynCaptcha captcha) {
		this.captcha = captcha;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void handleCaptcha(InventoryClickEvent event) {
		boolean hotbar = false;
		switch (event.getClick()) {
			case NUMBER_KEY:
				hotbar = true;
				break;
			case LEFT:
			case RIGHT:
				if (event.getCursor() == null || event.getCursor().getType() == Material.AIR
						|| event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
					return;
				}
				break;
			case CONTROL_DROP:
			case CREATIVE:
			case DOUBLE_CLICK:
			case DROP:
			case MIDDLE:
			case SHIFT_LEFT:
			case SHIFT_RIGHT:
			case WINDOW_BORDER_LEFT:
			case WINDOW_BORDER_RIGHT:
			case UNKNOWN:
			default:
				return;
		}
		ItemStack blankCaptcha;
		ItemStack toCaptcha;
		if (hotbar) {
			blankCaptcha = event.getView().getBottomInventory().getItem(event.getHotbarButton());
			toCaptcha = event.getCurrentItem();
		} else {
			blankCaptcha = event.getCurrentItem();
			toCaptcha = event.getCursor();
		}

		if (!EasterlynCaptcha.isBlankCaptcha(blankCaptcha) || captcha.canNotCaptcha(toCaptcha) || EasterlynCaptcha.isBlankCaptcha(toCaptcha)) {
			return;
		}

		ItemStack captchaItem = captcha.getCaptchaForItem(toCaptcha);
		event.setResult(Event.Result.DENY);

		// Decrement captcha stack
		if (hotbar) {
			event.getView().getBottomInventory().setItem(event.getHotbarButton(), ItemUtil.decrement(blankCaptcha, 1));
			event.setCurrentItem(null);
		} else {
			event.setCurrentItem(ItemUtil.decrement(blankCaptcha, 1));
			event.setCursor(null);
		}

		// Add to bottom inventory first
		int leftover = ItemUtil.getAddFailures(event.getView().getBottomInventory().addItem(captchaItem));
		if (leftover > 0) {
			// Add to top, bottom was full.
			leftover = ItemUtil.getAddFailures(event.getView().getTopInventory().addItem(captchaItem));
		}
		if (leftover > 0) {
			if (hotbar) {
				// Drop rather than delete (Items can be picked up before event completes, thanks Bukkit.)
				event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), captchaItem);
			} else {
				// Set cursor to captcha
				//noinspection deprecation
				event.setCursor(captchaItem);
			}
		}
		((Player) event.getWhoClicked()).updateInventory();
	}

}
