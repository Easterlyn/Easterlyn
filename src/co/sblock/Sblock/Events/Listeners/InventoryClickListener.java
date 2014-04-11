package co.sblock.Sblock.Events.Listeners;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Machines.MachineInventoryTracker;
import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Machines.Type.Computer;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.Machines.Type.MachineType;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.Captchadex;
import co.sblock.Sblock.Utilities.Server.ServerMode;

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
		InventoryHolder ih = event.getView().getTopInventory().getHolder();

		Machine m;
		if (ih != null && ih instanceof Machine) {
			m = (Machine) ih;
			if (m != null) {
				event.setCancelled(m.handleClick(event));
				return;
			}
		}

		// Finds inventories other than chests opened by Machines
		if (ih != null && ih instanceof BlockState) {
			m = SblockMachines.getMachines().getManager().getMachineByBlock(((BlockState) ih).getBlock());
			if (m != null) {
				event.setCancelled(m.handleClick(event));
				return;
			}
		}

		// Finds inventories forcibly opened by Machines
		m = MachineInventoryTracker.getTracker().getOpenMachine((Player) event.getWhoClicked());
		if (m != null) {
			event.setCancelled(m.handleClick(event));
			return;
		}

		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			// General cancellation: Books cannot be captcha'd, so this is easier
			// than trying to detect the Captchadex and prevent clicking it.
			if (event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD
					|| event.getAction() == InventoryAction.HOTBAR_SWAP) {
				if (event.getView().getBottomInventory().getItem(event.getHotbarButton()).getType()
						== Material.WRITTEN_BOOK) {
					event.setCancelled(true);
					return;
				}
			}
			if (event.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
				event.setCancelled(true);
				return;
			}
		}
		boolean top = event.getRawSlot() == event.getView().convertSlot(event.getRawSlot());
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
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
		}
	}

	// remove top
	@SuppressWarnings("deprecation")
	private void itemRemoveTop(InventoryClickEvent event) {
		if (event.getClickedInventory().getTitle().equals("Captchadex")) {
			if (event.getClick() == ClickType.LEFT) {
				event.setCurrentItem(Captchadex.itemToPunchcard(event.getCurrentItem()));
			} else {
				event.setResult(Result.DENY);
			}
		}
		// Server mode: Do not remove, clone to cursor.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			event.setCursor(event.getCurrentItem().clone());
			((Player) event.getWhoClicked()).updateInventory();
		}
	}

	// add top
	@SuppressWarnings("deprecation")
	private void itemAddTop(InventoryClickEvent event) {
		if (event.getClickedInventory().getTitle().equals("Captchadex")) {
			if (!Captcha.isPunch(event.getCursor()) || event.getCursor().getAmount() > 1) {
				event.setResult(Result.DENY);
				return;
			}
			// TODO verify that this works
			event.setCursor(Captcha.captchaToItem(event.getCursor()));
			((Player) event.getWhoClicked()).updateInventory();
		}
		// Server mode: Do not add, delete.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			event.setCursor(null);
			((Player) event.getWhoClicked()).updateInventory();
		}
	}

	// move top to bottom
	private void itemShiftTopToBottom(InventoryClickEvent event) {
		if (event.getClickedInventory().getTitle().equals("Captchadex")) {
			event.setCurrentItem(Captchadex.itemToPunchcard(event.getCurrentItem()));
		}
		// Server mode: Do not move, clone and add.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			event.getWhoClicked().getInventory().addItem(event.getCurrentItem().clone());
		}
	}

	// switch top
	@SuppressWarnings("deprecation")
	private void itemSwapIntoTop(InventoryClickEvent event) {
		if (!event.getClickedInventory().getTitle().equals("Captchadex")
				&& Captcha.isBlankCaptcha(event.getCurrentItem())) {
			event.setResult(Result.DENY);
			// Could instead verify swap in is single punchcard,
			// but not really worth the bother - rare scenario.
		}
		// Server mode: Do not swap, delete.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			event.setCursor(null);
			((Player) event.getWhoClicked()).updateInventory();
		}
	}

	// remove bottom
	private void itemRemoveBottom(InventoryClickEvent event) {

		// Server: Click computer icon -> open computer interface
		if (SblockUser.getUser(event.getWhoClicked().getName()).isServer()) {
			if (event.getCurrentItem().equals(MachineType.COMPUTER.getUniqueDrop())) {
				// Right click air: Open computer
				event.setCancelled(true);
				event.getWhoClicked().openInventory(new Computer(event.getWhoClicked().getLocation(),
						event.getWhoClicked().getName()).getInventory());
			}
		}
	}

	// add bottom
	private void itemAddBottom(InventoryClickEvent event) {
		
	}

	// move bottom to top
	@SuppressWarnings("deprecation")
	private void itemShiftBottomToTop(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			if (Captcha.isPunch(event.getCurrentItem())
					&& event.getCurrentItem().getAmount() == 1) {
				event.setCurrentItem(Captcha.captchaToItem(event.getCurrentItem()));
			} else {
				event.setResult(Result.DENY);
			}
		}
		// Server mode: Do not move, delete.
		if (SblockUser.getUser(event.getWhoClicked().getName()).isServer()) {
			event.setResult(Result.DENY);
			// Do not delete Computer icon.
			if (!event.getCurrentItem().equals(MachineType.COMPUTER.getUniqueDrop())) {
				event.setCurrentItem(null);
				((Player) event.getWhoClicked()).updateInventory();
			}
		}
	}

	// switch bottom
	@SuppressWarnings("deprecation")
	private void itemSwapIntoBottom(InventoryClickEvent event) {
		if (Captcha.isBlankCaptcha(event.getCurrentItem())) {
			if (event.getCursor().getType() == Material.BOOK_AND_QUILL
					|| event.getCursor().getType() == Material.WRITTEN_BOOK
					|| (event.getCursor().hasItemMeta() && event.getCursor().getItemMeta().hasDisplayName()
							&& event.getCursor().getItemMeta().getDisplayName().equals("Captchacard")
							&& event.getCursor().getItemMeta().hasLore())) {
				// Invalid captcha objects
				return;
			}
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

		// Server: No picking up computer icon
		if (SblockUser.getUser(event.getWhoClicked().getName()).isServer()) {
			if (event.getCurrentItem().equals(MachineType.COMPUTER.getUniqueDrop())) {
				event.setCancelled(true);
			}
		}
	}

	// hotbar with inv
	private void itemSwapToHotbar(InventoryClickEvent event) {
		if (SblockUser.getUser(event.getWhoClicked().getName()).isServer()) {
			if (event.getCurrentItem().equals(MachineType.COMPUTER.getUniqueDrop())) {
				event.setCancelled(true);
			}
		}
	}
}
