package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.sblock.machines.MachineInventoryTracker;
import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Computer;
import co.sblock.machines.type.Machine;
import co.sblock.machines.type.MachineType;
import co.sblock.users.User;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.captcha.Captchadex;
import co.sblock.utilities.inventory.InventoryUtils;
import co.sblock.utilities.progression.ServerMode;

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

		boolean top = event.getRawSlot() == event.getView().convertSlot(event.getRawSlot());
		switch (event.getAction()) {
		case COLLECT_TO_CURSOR:
			itemGather(event);
			break;
		case HOTBAR_MOVE_AND_READD:
			itemShiftTopToBottom(event);
			if (!event.isCancelled()) {
				itemSwapToHotbar(event);
			}
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
		// Captchadex
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			// Screw it, this is too complex with Bukkit's limited API
			event.setResult(Result.DENY);
			return;
		}

		// Server mode: No gathering to cursor.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			return;
		}
	}

	// remove top
	@SuppressWarnings("deprecation")
	private void itemRemoveTop(InventoryClickEvent event) {
		// Captchadex
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			if (event.getClick() == ClickType.LEFT) {
				event.setCurrentItem(Captchadex.itemToPunchcard(event.getCurrentItem()));
			} else {
				event.setResult(Result.DENY);
			}
			return;
		}

		// Server mode: Do not remove, clone to cursor.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			event.setCursor(event.getCurrentItem().clone());
			((Player) event.getWhoClicked()).updateInventory();
			return;
		}
	}

	// add top
	@SuppressWarnings("deprecation")
	private void itemAddTop(InventoryClickEvent event) {
		// Cruxite items should not be tradeable.
		if (event.getCursor() != null && event.getCursor().getItemMeta().hasDisplayName()
				&& event.getCursor().getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "Cruxite ")) {
			event.setCancelled(true);
			return;
		}

		// Captchadex
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			if (!Captcha.isPunch(event.getCursor()) || event.getCursor().getAmount() > 1) {
				event.setResult(Result.DENY);
				return;
			}
			event.setCursor(Captcha.captchaToItem(event.getCursor()));
			((Player) event.getWhoClicked()).updateInventory();
			return;
		}

		// Server mode: Do not add, delete.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			event.setCursor(null);
			((Player) event.getWhoClicked()).updateInventory();
			return;
		}

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(event.getCursor())) {
			event.setResult(Result.DENY);
		}
	}

	// move top to bottom
	private void itemShiftTopToBottom(InventoryClickEvent event) {
		// Captchadex
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			event.setCurrentItem(Captchadex.itemToPunchcard(event.getCurrentItem()));
			return;
		}

		// Server mode: Do not move, clone and add.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			event.getWhoClicked().getInventory().addItem(event.getCurrentItem().clone());
			return;
		}
	}

	// switch top
	@SuppressWarnings("deprecation")
	private void itemSwapIntoTop(InventoryClickEvent event) {
		// Cruxite items should not be tradeable.
		if (event.getCursor() != null && event.getCursor().getItemMeta().hasDisplayName()
				&& event.getCursor().getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "Cruxite ")) {
			event.setCancelled(true);
			return;
		}

		// Captchadex
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			event.setResult(Result.DENY);
			// Could instead verify swap in is single punchcard,
			// but not really worth the bother - rare scenario.
			return;
		}

		// Server mode: Do not swap, delete.
		if (event.getView().getTopInventory().getHolder() instanceof ServerMode) {
			event.setResult(Result.DENY);
			event.setCursor(null);
			((Player) event.getWhoClicked()).updateInventory();
			return;
		}

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(event.getCursor())) {
			event.setResult(Result.DENY);
			return;
		}

		// Captcha: attempt to captcha item on cursor
		Captcha.handleCaptcha(event);
	}

	// remove bottom
	private void itemRemoveBottom(InventoryClickEvent event) {

		// Server: Click computer icon -> open computer interface
		if (User.getUser(event.getWhoClicked().getUniqueId()).isServer()) {
			if (event.getCurrentItem().equals(MachineType.COMPUTER.getUniqueDrop())) {
				// Right click air: Open computer
				event.setCancelled(true);
				event.getWhoClicked().openInventory(new Computer(event.getWhoClicked().getLocation(),
						event.getWhoClicked().getUniqueId().toString(), true)
								.getInventory(User.getUser(event.getWhoClicked().getUniqueId())));
			}
			return;
		}
	}

	// add bottom
	private void itemAddBottom(InventoryClickEvent event) {
		
	}

	// move bottom to top
	@SuppressWarnings("deprecation")
	private void itemShiftBottomToTop(InventoryClickEvent event) {
		// Cruxite items should not be tradeable.
		if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta().hasDisplayName()
				&& event.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "Cruxite ")) {
			event.setCancelled(true);
			return;
		}

		// Captchadex: convert single punchcard to item inside
		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			if (Captcha.isPunch(event.getCurrentItem())
					&& event.getCurrentItem().getAmount() == 1) {
				event.setCurrentItem(Captcha.captchaToItem(event.getCurrentItem()));
			} else {
				event.setResult(Result.DENY);
			}
			return;
		}

		// Server mode: Do not move, delete.
		if (User.getUser(event.getWhoClicked().getUniqueId()).isServer()) {
			event.setResult(Result.DENY);
			// Do not delete Computer icon.
			if (!event.getCurrentItem().equals(MachineType.COMPUTER.getUniqueDrop())) {
				event.setCurrentItem(null);
				((Player) event.getWhoClicked()).updateInventory();
			}
			return;
		}

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(event.getCurrentItem())) {
			event.setResult(Result.DENY);
			return;
		}
	}

	// switch bottom
	private void itemSwapIntoBottom(InventoryClickEvent event) {
		// Server: No picking up computer icon
		if (User.getUser(event.getWhoClicked().getUniqueId()).isServer()
				&& event.getCurrentItem().equals(MachineType.COMPUTER.getUniqueDrop())) {
			event.setCancelled(true);
			return;
		}

		// Captcha: attempt to captcha item on cursor
		Captcha.handleCaptcha(event);
	}

	// hotbar with inv
	private void itemSwapToHotbar(InventoryClickEvent event) {
		ItemStack hotbar = event.getView().getBottomInventory().getItem(event.getHotbarButton());

		if (User.getUser(event.getWhoClicked().getUniqueId()).isServer()
				&& (event.getCurrentItem().equals(MachineType.COMPUTER.getUniqueDrop())
						|| hotbar.equals(MachineType.COMPUTER.getUniqueDrop()))) {
			event.setCancelled(true);
			return;
		}

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& (InventoryUtils.isUniqueItem(event.getCursor())
						|| InventoryUtils.isUniqueItem(hotbar))) {
			event.setResult(Result.DENY);
			return;
		}

		if (event.getView().getTopInventory().getTitle().equals("Captchadex")) {
			// General cancellation: Books cannot be captcha'd. Faster than detecting Captchadex.
			if (hotbar.getType() == Material.WRITTEN_BOOK
					|| event.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
				event.setCancelled(true);
				return;
			}
		}

		// TODO ENTRY

		// Captcha: attempt to captcha item in clicked slot
		Captcha.handleCaptcha(event);
	}
}
