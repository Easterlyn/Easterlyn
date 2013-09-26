package co.sblock.Sblock.Utilities.Captcha;

import org.bukkit.Bukkit;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Utilities.Sblogger;

public class CaptchaEventListener implements Listener	{

//	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event) {
		if (event.getWhoClicked() == null) {
			// Certain sections appear to return this - active effects and non-slot areas
			return;
		}
		if (event.getClickedInventory() == null
				|| event.getResult().equals(Result.DENY)
				|| event.getClickedInventory().getType() != InventoryType.CRAFTING
				&& event.getClickedInventory().getType() != InventoryType.WORKBENCH) {
			return;
		}
		switch (event.getAction()) {
		case PLACE_ALL:
		case PLACE_ONE:
		case PLACE_SOME:
		case SWAP_WITH_CURSOR:
			handleCaptchaCraftPrep(event);
			return;
		case MOVE_TO_OTHER_INVENTORY:
		case COLLECT_TO_CURSOR:
			// Does not appear to work in crafting inventory - TODO verify
			// - may be worth keeping in case of future updates
		case PICKUP_ALL:
			// These cases could result in a recipe being formed correctly.
			if (event.getSlot() != 0 && handleCaptchaCraftPrep(event)) {
				return;
			}
		case PICKUP_ONE:
		case PICKUP_HALF:
		case PICKUP_SOME:
			// PICKUP_SOME and PICKUP_ONE do not seem to fire, but just in case..
			if (event.getSlot() != 0) {
				// Not a crafting event or already handled.
				return;
			}
			ItemStack card = captchaRecipe(event.getClickedInventory().getContents());
			if (card == null)
				return;
			if (!card.equals(Captcha.itemToCaptcha(event.getClickedInventory().getItem(0)))) {
				return;
			}
			if (event.getClickedInventory().contains(card)) {
				event.getClickedInventory().remove(card);
				if (!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
					event.getWhoClicked().setItemOnCursor(event.getClickedInventory().getItem(0));
					event.getClickedInventory().clear(0);
				}
			}
			return;
		case CLONE_STACK:
		case DROP_ALL_CURSOR:
		case DROP_ALL_SLOT:
		case DROP_ONE_CURSOR:
		case DROP_ONE_SLOT:
		case HOTBAR_MOVE_AND_READD:
		case HOTBAR_SWAP:
		case NOTHING:
		case UNKNOWN:
		default:
			break;
		}
	}

	private boolean handleCaptchaCraftPrep(InventoryClickEvent event) {
		debug(event.getClickedInventory());
		ItemStack output = null;
		switch (event.getAction()) {
		case PLACE_ALL:
		case PLACE_ONE:
		case PLACE_SOME:
		case SWAP_WITH_CURSOR:
			if (!isCaptchaPrep(event.getClickedInventory())) {
				// TODO fix swap ^
				return false;
			}
			if (event.getSlot() == 0) {
				return false;
			}
			ItemStack is = event.getCursor();
			if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()
					|| !is.getItemMeta().getDisplayName().equals("Captchacard")
					|| !is.getItemMeta().hasLore()
					|| is.getItemMeta().getLore().contains("Blank")) {
				return false;
			}
			output = Captcha.captchaToItem(is);
			break;
		case MOVE_TO_OTHER_INVENTORY:
		case COLLECT_TO_CURSOR:
		case PICKUP_ALL:
			ItemStack[] invContents = event.getClickedInventory().getContents();
			invContents[event.getSlot()] = null;
			output = captchaRecipe(invContents);
			if (output == null) {
				return false;
			}
			break;
		default:
			return false;
		}
		event.getClickedInventory().setItem(0, output);
		if (event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
			ItemStack temp = event.getCurrentItem();
			event.getClickedInventory().setItem(event.getSlot(), event.getCursor());
			event.getWhoClicked().setItemOnCursor(temp);
		} else if (event.getAction().equals(InventoryAction.PLACE_ALL)) {
			event.getClickedInventory().setItem(event.getSlot(), event.getCursor());
			event.getWhoClicked().setItemOnCursor(null);
		} else if (event.getAction().equals(InventoryAction.PLACE_SOME)) {
			// TODO better way? Is event fired at end of right/left click spread, during, etc?
			ItemStack temp = event.getCursor().clone();
			temp.setAmount(temp.getAmount() / 2);
			event.getClickedInventory().setItem(event.getSlot(), temp);
			temp.setAmount(event.getCursor().getAmount() - temp.getAmount());
			// Account for odd numbers rounding down
			event.getWhoClicked().setItemOnCursor(temp);
		} else if (event.getAction().equals(InventoryAction.PLACE_ONE)) {
			ItemStack temp = event.getCursor().clone();
			temp.setAmount(1);
			event.getClickedInventory().setItem(event.getSlot(), temp);
			temp.setAmount(event.getCursor().getAmount() - 1);
			event.getWhoClicked().setItemOnCursor(temp);
		}
		// Hacky solution attempt
		// Explanation: UpdateInventory doesn't work (it only affects player inventory)
		// and there's no way to access player crafting grid at all aside from this event.
		//     To fix this, we open a new workbench crafting grid with the contents of the previous
		// crafting inventory. Items on the cursor and in the grid will be dropped on grid close, so we
		// record and remove them. When we create the new grid, we re-add all the items in their slots
		// (this will ruin shaped recipes, TODO account for), open the inventory, and set the item on the
		// cursor to what it was (if anything).
		ItemStack[] contents = event.getClickedInventory().getContents();
		event.getClickedInventory().clear();
		ItemStack cursor = event.getCursor();
		if (cursor != null) {
			event.getWhoClicked().setItemOnCursor(null);
		}
		Inventory newInv = Bukkit.createInventory(event.getWhoClicked(), InventoryType.WORKBENCH);
		if (event.getClickedInventory().getType().equals(InventoryType.WORKBENCH)) {
			newInv.setContents(contents);
		} else {
			for (int i = 0; i < contents.length; i++) {
				if (contents[i] != null) {
					newInv.setItem(i, contents[i]);
				}
			}
		}
		event.getWhoClicked().openInventory(newInv);
		event.getWhoClicked().setItemOnCursor(cursor);
		return true;
	}

	private boolean isCaptchaPrep(Inventory i) {
		// TODO:
		// Crafting of captcha? Or villager inv?
		// removal of crafting supplies for captcha
		int stacks = 0;
//		ItemStack captcha = null;
//		if (i.getItem(0) != null) {
//			// Crafting recipe for something else, we're outta here.
//			return false;
//		}
		for (int j = 1; j < i.getSize(); j++) {
			ItemStack is = i.getItem(j);
			if (is != null) {
				stacks++;
			}
		}
		// If there are other items, the recipe is wrong, even if they are all valid Captchacards.
		return stacks == 0 ? true : false;
	}

	private ItemStack captchaRecipe(ItemStack[] i) {
		int stacks = 0;
		ItemStack captcha = null;
		for (int j = 1; j < i.length; j++) {
			ItemStack is = i[j];
			if (is != null) {
				if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()
						|| !is.getItemMeta().getDisplayName().equals("Captchacard")
						|| !is.getItemMeta().hasLore()
						|| is.getItemMeta().getLore().contains("Blank")) {
					// Not a Captchacard, crafting recipe is wrong.
					return null;
				}
				// Captchacard is valid.
				captcha = Captcha.captchaToItem(is);
				stacks++;
			}
		}
		// If there are other items, the recipe is wrong, even if they are all valid Captchacards.
		return stacks == 1 ? captcha : null;
	}

	private void debug(Inventory i) {
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < i.getSize(); j++) {
			sb.append(j).append(":").append(i.getItem(j) != null ? i.getItem(j).getType().toString() : "null").append(" ");
		}
		Sblogger.info("Inv Debug", sb.toString().substring(0, sb.length() - 1));
	}

	/*
	 * 
	 * Just in case we need this later, here it is.
	 */
	@SuppressWarnings("unused")
	private int getRelative(InventoryType it, Direction d, int current) {
		switch (it) {
		case CRAFTING:
			switch (d) {
			case UP:
				return current - 2 > 0 ? current - 2 : -1;
			case DOWN:
				return current + 2 < 4 ? current + 2 : -1;
			case LEFT:
				return (current == 2 || current == 4) ? current - 1 : -1;
			case RIGHT:
				return (current == 1 || current == 3) ? current + 1 : -1;
			}
		case WORKBENCH:
			switch (d) {
			case UP:
				return current - 3 > 0 ? current - 3 : -1;
			case DOWN:
				return current + 3 < 4 ? current + 3 : -1;
			case LEFT:
				return (current != 1 || current != 4 || current != 7) ? current - 1 : -1;
			case RIGHT:
				return current % 3 != 0 ? current + 1  : -1;
			}
		default:
		return -1;
		}
	}

	enum Direction {
		UP, DOWN, LEFT, RIGHT;
	}
	/* 12
	 * 34
	 * 
	 * 123
	 * 456
	 * 789
	 */
}
