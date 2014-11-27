package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.machines.MachineInventoryTracker;
import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Computer;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.MachineType;
import co.sblock.users.UserManager;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.inventory.InventoryUtils;

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
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
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

		// Finds inventories of physical blocks opened by Machines
		if (ih != null && ih instanceof BlockState) {
			m = SblockMachines.getInstance().getMachineByBlock(((BlockState) ih).getBlock());
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
		switch (event.getClick()) {
		case DOUBLE_CLICK:
			itemGather(event);
			break;
		case NUMBER_KEY:
			if (top) {
				itemShiftTopToBottom(event);
			}
			if (!event.isCancelled()) {
				itemSwapToHotbar(event);
			}
			break;
		case LEFT:
		case RIGHT:
			if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
				if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
					return;
				}
				if (top) {
					itemRemoveTop(event);
				} else {
					itemRemoveBottom(event);
				}
			} else if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
				if (top) {
					itemAddTop(event);
				} else {
					itemAddBottom(event);
				}
			} else {
				if (top) {
					itemSwapIntoTop(event);
				} else {
					itemSwapIntoBottom(event);
				}
			}
			break;
		case SHIFT_LEFT:
		case SHIFT_RIGHT:
			if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
				break;
			}
			if (top) {
				itemShiftTopToBottom(event);
			} else {
				itemShiftBottomToTop(event);
			}
			break;
		case CONTROL_DROP:
		case DROP:
		case WINDOW_BORDER_LEFT:
		case WINDOW_BORDER_RIGHT:
			if (top) {
				itemRemoveTop(event);
			} else {
				itemRemoveBottom(event);
			}
			break;
		case CREATIVE:
		case MIDDLE:
		case UNKNOWN:
		default:
			return;
		}
	}

	// doubleclick gather
	private void itemGather(InventoryClickEvent event) {}

	// remove top
	private void itemRemoveTop(InventoryClickEvent event) {}

	// add top
	private void itemAddTop(InventoryClickEvent event) {
		// Cruxite items should not be tradeable.
		if (event.getCursor() != null && event.getCursor().getItemMeta().hasDisplayName()
				&& event.getCursor().getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "Cruxite ")) {
			event.setCancelled(true);
			return;
		}

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(event.getCursor())) {
			event.setResult(Result.DENY);
		}
	}

	// move top to bottom
	private void itemShiftTopToBottom(InventoryClickEvent event) {}

	// switch top
	private void itemSwapIntoTop(InventoryClickEvent event) {
		// Cruxite items should not be tradeable.
		if (event.getCursor() != null && event.getCursor().getItemMeta().hasDisplayName()
				&& event.getCursor().getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "Cruxite ")) {
			event.setCancelled(true);
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
		if (UserManager.getUser(event.getWhoClicked().getUniqueId()).isServer()) {
			if (event.getCurrentItem().isSimilar(MachineType.COMPUTER.getUniqueDrop())) {
				// Right click air: Open computer
				event.setCancelled(true);
				event.getWhoClicked().openInventory(new Computer(event.getWhoClicked().getLocation(),
						event.getWhoClicked().getUniqueId().toString(), true)
								.getInventory(UserManager.getUser(event.getWhoClicked().getUniqueId())));
			}
			return;
		}
	}

	// add bottom
	private void itemAddBottom(InventoryClickEvent event) {}

	// move bottom to top
	private void itemShiftBottomToTop(InventoryClickEvent event) {
		// Cruxite items should not be tradeable.
		if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta().hasDisplayName()
				&& event.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "Cruxite ")) {
			event.setCancelled(true);
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
		if (UserManager.getUser(event.getWhoClicked().getUniqueId()).isServer()
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

		if (UserManager.getUser(event.getWhoClicked().getUniqueId()).isServer()
				&& (event.getCurrentItem().isSimilar(MachineType.COMPUTER.getUniqueDrop())
						|| hotbar.isSimilar(MachineType.COMPUTER.getUniqueDrop()))) {
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

		// TODO ENTRY

		// Captcha: attempt to captcha item in clicked slot
		Captcha.handleCaptcha(event);
	}

	/**
	 * EventHandler for inventory clicks that are guaranteed to have occurred.
	 * 
	 * @param event
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryClickHasOccurred(InventoryClickEvent event) {
		final Inventory toTest = event.getInventory();

		if (toTest.getType() != InventoryType.ANVIL
				|| !((Player) event.getWhoClicked()).hasPermission("sblock.blaze")) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				doCombine((AnvilInventory) toTest);
			}
		}.runTask(Sblock.getInstance());
	}

	private void doCombine(AnvilInventory toTest) {
		ItemStack firstSlot = toTest.getItem(0);
		ItemStack secondSlot = toTest.getItem(1);

		if (firstSlot == null || secondSlot == null || firstSlot.getType() != Material.SADDLE
				|| secondSlot.getType() != Material.ENCHANTED_BOOK) {
			return;
		}

		ItemStack maybeSaddle = tryCombineBookSaddle(secondSlot, firstSlot);

		if (maybeSaddle != null) {
			toTest.setItem(2, maybeSaddle);
		}
	}

	private ItemStack tryCombineBookSaddle(ItemStack book, ItemStack saddle) {
		int fireAspectLevel = 0;

		if (book.getItemMeta() instanceof EnchantmentStorageMeta) {
			EnchantmentStorageMeta esm = (EnchantmentStorageMeta) book.getItemMeta();

			fireAspectLevel = esm.getStoredEnchantLevel(Enchantment.ARROW_FIRE);
		}

		if (fireAspectLevel > 0) {
			ItemStack blazingSaddle = new ItemStack(Material.SADDLE, 1);
			blazingSaddle.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
			return blazingSaddle;
		}
		return null;
	}
}
