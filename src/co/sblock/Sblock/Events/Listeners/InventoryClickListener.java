package co.sblock.Sblock.Events.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.Captchadex;

/**
 * Listener for InventoryClickEvents.
 * 
 * @author Jikoo
 */
public class InventoryClickListener implements Listener {

	/**
	 * EventHandler for all InventoryClickEvents.
	 * 
	 * @param event the InventoryClickEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		//Adam check for totem lathe furnace
		InventoryHolder ih = event.getView().getTopInventory().getHolder();
		if (ih != null && (ih instanceof Machine)) {
			Machine m = (Machine) ih;
			if (m != null) {
				event.setCancelled(m.handleClick(event));
				return;
			}
		}
		boolean top = event.getView().getTopInventory().equals(event.getClickedInventory());
		switch (event.getAction()) {
		case COLLECT_TO_CURSOR:
			itemGather(event);
			break;
		case HOTBAR_MOVE_AND_READD:
			itemShiftTopToBottom(event);
			break;
		case MOVE_TO_OTHER_INVENTORY:
			if (top) {
				itemShiftTopToBottom(event);
			} else {
				itemShiftBottomToTop(event);
			}
			break;
		case HOTBAR_SWAP:
			itemSwapToHotbar(event);
			break;
		case PICKUP_ALL:
		case PICKUP_HALF:
		case PICKUP_ONE:
		case PICKUP_SOME:
			if (top) {
				itemRemoveTop(event);
			} else {
				itemRemoveBottom(event);
			}
			break;
		case PLACE_ALL:
		case PLACE_ONE:
		case PLACE_SOME:
			if (top) {
				itemAddTop(event);
			} else {
				itemAddBottom(event);
			}
			break;
		case SWAP_WITH_CURSOR:
			if (top) {
				itemSwapIntoTop(event);
			} else {
				itemSwapIntoBottom(event);
			}
			break;
		case CLONE_STACK:
		case DROP_ALL_CURSOR:
		case DROP_ALL_SLOT:
		case DROP_ONE_CURSOR:
		case DROP_ONE_SLOT:
		case NOTHING:
		case UNKNOWN:
		default:
			break;
		}
	}

	// doubleclick gather
	private void itemGather(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			// Screw it, this is too complex with Bukkit's limited API
			event.setResult(Result.DENY);
		}
	}

	// remove top
	private void itemRemoveTop(InventoryClickEvent event) {
		if (event.getClickedInventory().getTitle().equals("Captchadex")) {
			if (event.getClick() == ClickType.LEFT) {
				event.setCurrentItem(Captchadex.itemToCard(event.getCurrentItem()));
			} else {
				// Adam fix PICKUP_HALF
				event.setResult(Result.DENY);
			}
		}
	}

	// add top
	@SuppressWarnings("deprecation")
	private void itemAddTop(InventoryClickEvent event) {
		if (event.getClickedInventory().getTitle().equals("Captchadex")) {
			if (!Captcha.isPunchCard(event.getCursor()) || event.getCursor().getAmount() > 1) {
				event.setResult(Result.DENY);
				return;
			}
			// TODO verify that this works
			event.setCursor(Captcha.captchaToItem(event.getCursor()));
			((Player) event.getWhoClicked()).updateInventory();
			// Old working code:
//			ItemStack cursor = e.getCursor().clone();
//			ItemStack[] contents = clickedInv.getContents();
//			e.setCursor(null);
//			Player p = (Player) e.getWhoClicked();
//			Inventory i = Captchadex.createCaptchadexInventory(p);
//			p.closeInventory();
//			i.setContents(contents);
//			i.addItem(Captchadex.punchCardToItem(cursor));
//			p.openInventory(i);
//			p.updateInventory();
		}
	}

	// move top to bottom
	private void itemShiftTopToBottom(InventoryClickEvent event) {
		if (event.getClickedInventory().getTitle().equals("Captchadex")) {
			event.setCurrentItem(Captchadex.itemToCard(event.getCurrentItem()));
		}
	}

	// switch top
	private void itemSwapIntoTop(InventoryClickEvent event) {
		if (!event.getClickedInventory().getTitle().equals("Captchadex")
				&& Captcha.isBlankCard(event.getCurrentItem())) {
			event.setResult(Result.DENY);
			// Could instead verify swap in is single punchcard,
			// but not really worth the bother - rare scenario.
		}
	}

	// remove bottom
	private void itemRemoveBottom(InventoryClickEvent event) {
		
	}

	// add bottom
	private void itemAddBottom(InventoryClickEvent event) {
		
	}

	// move bottom to top
	private void itemShiftBottomToTop(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			if (Captcha.isPunchCard(event.getCurrentItem())
					&& event.getCurrentItem().getAmount() == 1) {
				event.setCurrentItem(Captcha.captchaToItem(event.getCurrentItem()));
			} else {
				event.setResult(Result.DENY);
			}
		}
	}

	// switch bottom
	@SuppressWarnings("deprecation")
	private void itemSwapIntoBottom(InventoryClickEvent event) {
		if (Captcha.isBlankCard(event.getCurrentItem())) {
			if (event.getCursor().getType() == Material.BOOK_AND_QUILL
					|| event.getCursor().getType() == Material.WRITTEN_BOOK
					|| (event.getCursor().hasItemMeta() && event.getCursor().getItemMeta().hasDisplayName()
							&& event.getCursor().getItemMeta().getDisplayName().equals("Captchacard")
							&& event.getCursor().getItemMeta().hasLore())) {
				// Invalid captcha objects
				return;
			}
			// Adam verify that modified code works
			Player p = (Player) event.getWhoClicked();
			ItemStack captcha = Captcha.itemToCaptcha(event.getCursor());
			event.setCursor(null);
			event.setResult(Result.DENY);;
			if (event.getCurrentItem().getAmount() > 1) {
				event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
				if (p.getInventory().firstEmpty() != -1) {
					p.getInventory().addItem(captcha);
				} else {
					event.setCursor(captcha);
				}
			} else {
				event.setCurrentItem(captcha);
			}
			p.updateInventory();
		}
	}

	// hotbar with inv
	private void itemSwapToHotbar(InventoryClickEvent event) {
		
	}
}
