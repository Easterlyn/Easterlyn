package co.sblock.events.listeners.inventory;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.micromodules.AwayFromKeyboard;
import co.sblock.utilities.InventoryUtils;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for InventoryClickEvents.
 * 
 * @author Jikoo
 */
public class InventoryClickListener extends SblockListener {

	private final AwayFromKeyboard afk;
	private final Captcha captcha;
	private final Machines machines;

	public InventoryClickListener(Sblock plugin) {
		super(plugin);
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.captcha = plugin.getModule(Captcha.class);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for all InventoryClickEvents.
	 * 
	 * @param event the InventoryClickEvent
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		InventoryHolder ih = event.getView().getTopInventory().getHolder();

		// Finds inventories of physical blocks opened by Machines
		if (ih != null && ih instanceof BlockState) {
			Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(((BlockState) ih).getBlock());
			if (pair != null) {
				event.setCancelled(pair.getLeft().handleClick(event, pair.getRight()));
			}
		}

		// Finds inventories forcibly opened by Machines
		Pair<Machine, ConfigurationSection> pair = machines.getInventoryTracker().getOpenMachine((Player) event.getWhoClicked());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleClick(event, pair.getRight()));
			return;
		}

		// Lowest priority Machine check, one with no identifying block
		Machine m;
		if (ih != null && ih instanceof Machine) {
			m = (Machine) ih;
			if (m != null) {
				event.setCancelled(m.handleClick(event, null));
				return;
			}
		}

		boolean top = event.getRawSlot() == event.getView().convertSlot(event.getRawSlot());
		switch (event.getClick()) {
		case DOUBLE_CLICK:
			itemGather(event);
			if (event.isCancelled()) {
				break;
			}
			if (top) {
				itemRemoveTop(event);
			} else {
				itemRemoveBottom(event);
			}
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

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(getPlugin(), event.getCursor())) {
			event.setResult(Result.DENY);
		}

	}

	// move top to bottom
	private void itemShiftTopToBottom(InventoryClickEvent event) {}

	// switch top
	private void itemSwapIntoTop(InventoryClickEvent event) {

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(getPlugin(), event.getCursor())) {
			event.setResult(Result.DENY);
			return;
		}

		// Captcha: attempt to captcha item on cursor
		captcha.handleCaptcha(event);
	}

	// remove bottom
	private void itemRemoveBottom(InventoryClickEvent event) {}

	// add bottom
	private void itemAddBottom(InventoryClickEvent event) {}

	// move bottom to top
	private void itemShiftBottomToTop(InventoryClickEvent event) {

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& InventoryUtils.isUniqueItem(getPlugin(), event.getCurrentItem())) {
			event.setResult(Result.DENY);
			return;
		}
	}

	// switch bottom
	private void itemSwapIntoBottom(InventoryClickEvent event) {

		// Captcha: attempt to captcha item on cursor
		captcha.handleCaptcha(event);
	}

	// hotbar with inv
	private void itemSwapToHotbar(InventoryClickEvent event) {
		ItemStack hotbar = event.getView().getBottomInventory().getItem(event.getHotbarButton());

		// No putting special Sblock items into anvils, it'll ruin them.
		if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
				&& (InventoryUtils.isUniqueItem(getPlugin(), event.getCursor())
						|| InventoryUtils.isUniqueItem(getPlugin(), hotbar))) {
			event.setResult(Result.DENY);
			return;
		}

		// Captcha: attempt to captcha item in clicked slot
		captcha.handleCaptcha(event);
	}

	/**
	 * EventHandler for inventory clicks that are guaranteed to have occurred.
	 * 
	 * @param event
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryClickHasOccurred(InventoryClickEvent event) {

		// Extend non-afk status
		if (event.getWhoClicked() instanceof Player) {
			afk.extendActivity((Player) event.getWhoClicked());
		}

		final InventoryView view = event.getView();

		if (view.getTopInventory().getType() != InventoryType.ANVIL
				|| !((Player) event.getWhoClicked()).hasPermission("sblock.blaze")) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				createBlazingSaddle(view);
			}
		}.runTask(getPlugin());
	}

	private void createBlazingSaddle(InventoryView view) {
		Inventory top = view.getTopInventory();
		ItemStack firstSlot = top.getItem(0);
		ItemStack secondSlot = top.getItem(1);

		if (firstSlot == null || secondSlot == null || firstSlot.getType() != Material.SADDLE
				|| firstSlot.containsEnchantment(Enchantment.ARROW_FIRE)
				|| secondSlot.getType() != Material.ENCHANTED_BOOK || !secondSlot.hasItemMeta()) {
			return;
		}

		EnchantmentStorageMeta esm = (EnchantmentStorageMeta) secondSlot.getItemMeta();
		int fire = esm.getStoredEnchantLevel(Enchantment.ARROW_FIRE);

		if (fire < 1) {
			return;
		}

		ItemStack blazingSaddle = new ItemStack(firstSlot);
		ItemMeta saddleMeta = blazingSaddle.getItemMeta();

		saddleMeta.addEnchant(Enchantment.ARROW_FIRE, 1, true);

		Repairable repairable = (Repairable) saddleMeta;
		int cost = repairable.hasRepairCost() ? repairable.getRepairCost() : 0;

		// Next cost is always current * 2 + 1
		((Repairable) saddleMeta).setRepairCost(cost * 2 + 1);

		// Flame from a book costs 2
		cost += 2;

		String displayName = InventoryUtils.getNameFromAnvil(view);
		if (saddleMeta.hasDisplayName() && !saddleMeta.getDisplayName().equals(displayName)
				|| !saddleMeta.hasDisplayName() && displayName != null) {
			saddleMeta.setDisplayName(displayName);
			// Renaming adds 1
			cost += 1;
		}

		blazingSaddle.setItemMeta(saddleMeta);

		top.setItem(2, blazingSaddle);
		InventoryUtils.setAnvilExpCost(view, cost);
		if (view.getPlayer() instanceof Player) {
			((Player) view.getPlayer()).updateInventory();
		}
		InventoryUtils.updateAnvilExpCost(view);
	}

}
