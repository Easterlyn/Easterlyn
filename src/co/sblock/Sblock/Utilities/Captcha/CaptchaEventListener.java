package co.sblock.Sblock.Utilities.Captcha;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * @author Jikoo, Dublek
 */
public class CaptchaEventListener implements Listener	{

	/**
	 * The event handler for <code>Captcha</code>-related
	 * <code>InventoryClickEvents</code>.
	 * 
	 * @param e
	 *            the <code>InventoryClickEvent</code>
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.isCancelled() || e.getInventory() == null
				|| !(e.getWhoClicked() instanceof Player)
				|| e.getResult() == Result.DENY) {
			return;
		}
		Inventory clickedInv =  e.getView().getTopInventory().getSize() < e.getRawSlot()
				? ((Player) e.getWhoClicked()).getInventory() : e.getView().getTopInventory();
		if (e.getInventory().getTitle().equals("Captchadex")
				|| ((Player) e.getWhoClicked()).getOpenInventory().getTitle().equals("Captchadex")) {
			switch (e.getAction()) {
			case PICKUP_ALL:
				if (clickedInv.getTitle().equals("Captchadex")) {
					e.setCurrentItem(Captchadex.itemToCard(e.getCurrentItem()));
				}
				break;
			case PICKUP_HALF:
				if (clickedInv.getTitle().equals("Captchadex")) {
					e.setCurrentItem(Captchadex.itemToCard(e.getCurrentItem()));
				}
				break;
			case PICKUP_ONE:
				if (clickedInv.getTitle().equals("Captchadex")) {
					e.setCurrentItem(Captchadex.itemToCard(e.getCurrentItem()));
				}
				break;
			case PICKUP_SOME:
				if (clickedInv.getTitle().equals("Captchadex")) {
					e.setCurrentItem(Captchadex.itemToCard(e.getCurrentItem()));
				}
				break;
			case MOVE_TO_OTHER_INVENTORY:
				if (clickedInv.getTitle().equals("Captchadex")) {
					e.setCurrentItem(Captchadex.itemToCard(e.getCurrentItem()));
				} else {
					e.setResult(Result.DENY);
				}
				break;
			case PLACE_ALL:
				if(clickedInv.getTitle().equals("Captchadex"))	{
					if (Captcha.isSinglePunchCard(e.getCursor())) {
						ItemStack cursor = e.getCursor().clone();
						ItemStack[] contents = clickedInv.getContents();
						e.setCursor(null);
						Player p = (Player) e.getWhoClicked();
						Inventory i = Captchadex.createCaptchadexInventory(p);
						p.closeInventory();
						i.setContents(contents);
						i.addItem(Captchadex.punchCardToItem(cursor));
						p.openInventory(i);
						//p.setItemOnCursor(null);
						p.updateInventory();
					} else {
						e.setResult(Result.DENY);
					}
				}
				break;
			case PLACE_ONE:
				if(clickedInv.getTitle().equalsIgnoreCase("Captchadex")) {
					if (Captcha.isPunchCard(e.getCursor())) {
						ItemStack cursor = e.getCursor().clone();
						ItemStack[] contents = clickedInv.getContents();
						e.setCursor(null);
						Player p = (Player) e.getWhoClicked();
						Inventory i = Captchadex.createCaptchadexInventory(p);
						p.closeInventory();
						i.setContents(contents);
						i.addItem(Captchadex.punchCardToItem(cursor));
						p.openInventory(i);
						//p.setItemOnCursor(null);
						//p.updateInventory();
					} else {
						e.setResult(Result.DENY);
					}
				}
				break;
			case PLACE_SOME:
				if (clickedInv.getTitle().equalsIgnoreCase("Captchadex")) {
					if (Captcha.isPunchCard(e.getCursor())) {
						ItemStack cursor = e.getCursor().clone();
						ItemStack[] contents = clickedInv.getContents();
						e.setCursor(null);
						Player p = (Player) e.getWhoClicked();
						Inventory i = Captchadex.createCaptchadexInventory(p);
						p.closeInventory();
						i.setContents(contents);
						i.addItem(Captchadex.punchCardToItem(cursor));
						p.openInventory(i);
						//p.setItemOnCursor(null);
						//p.updateInventory();
					} else {
						e.setResult(Result.DENY);
					}
				}
				break;
			default:
				if (clickedInv.getTitle().equals("Captchadex")) {
					e.setResult(Result.DENY);
				}
			}
		}

		if (!(clickedInv.getType() == InventoryType.PLAYER)
				|| !(e.getAction() == InventoryAction.SWAP_WITH_CURSOR)) {
			return;
		}
		ItemStack is = e.getCurrentItem();
		if (is == null || !(is.getType() == Material.PAPER)
				|| !is.hasItemMeta()
				|| !is.getItemMeta().hasDisplayName()
				|| !is.getItemMeta().getDisplayName().equals("Captchacard")
				|| !is.getItemMeta().hasLore()
				|| !is.getItemMeta().getLore().contains("Blank")) {
			// Not a blank Captchacard
			return;
		}
		if (e.getCursor() == null) {
			// This shouldn't be possible.
			return;
		}
		ItemStack toCaptcha = e.getCursor().clone();
		if (toCaptcha.getType() == Material.BOOK_AND_QUILL
				|| toCaptcha.getType() == Material.WRITTEN_BOOK
				|| (toCaptcha.hasItemMeta() && toCaptcha.getItemMeta().hasDisplayName()
						&& toCaptcha.getItemMeta().getDisplayName().equals("Captchacard"))) {
			// Invalid captcha objects - Books store too much
			return;
		}
		// .. I am just SOOO worried about a little miscommunication at this point.
		e.setCursor(null);
		// Quaking in my boots, Bukkit.
		e.setCancelled(true);
		Player p = (Player) e.getWhoClicked();
		ItemStack captcha = Captcha.itemToCaptcha(toCaptcha);
		if (is.getAmount() > 1) {
			is.setAmount(is.getAmount() - 1);
			if (p.getInventory().firstEmpty() != -1) {
				p.getInventory().addItem(captcha);
			} else {
				p.getWorld().dropItem(p.getEyeLocation(), captcha)
						.setVelocity(p.getLocation().getDirection().multiply(0.4));
			}
		} else {
			clickedInv.setItem(e.getSlot(), captcha);
		}
		p.updateInventory();
	}

	/**
	 * The event handler for Captcha-related <code>InventoryDragEvent</code>s.
	 * 
	 * @param e
	 *            the <code>InventoryDragEvent</code>
	 */
	@EventHandler
	public void onItemDragDrop(InventoryDragEvent e) {
		if (e.isCancelled() || e.getInventory() == null
				|| !(e.getWhoClicked() instanceof Player)
				|| e.getResult() == Result.DENY) {
			return;
		}
		if (!e.getView().getTopInventory().getTitle().equals("Captchadex")) {
			return;
		}
		for (int i : e.getRawSlots()) {
			if (e.getView().getTopInventory().getSize() > i) {
				e.setResult(Result.DENY);
				break;
			}
		}
	}

	/**
	 * The event handler for Captcha-related <code>PlayerInteractEvents</code>
	 * (Uncaptcha-ing).
	 * 
	 * @param e
	 *            the <code>PlayerInteractEvent</code>
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.isCancelled() &&
				(e.getAction() != Action.LEFT_CLICK_AIR
				&& e.getAction() != Action.RIGHT_CLICK_AIR) ?
				true : e.useItemInHand() == Result.DENY) {
			// NOPE.AVI
			return;
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.hasBlock()
				&& hasRightClickFunction(e.getClickedBlock()) && !e.getPlayer().isSneaking()) {
			// Other inventory opening.
			return;
		}
		ItemStack is = e.getPlayer().getItemInHand();
		if (is.getType().equals(Material.WRITTEN_BOOK) 
				&& is.hasItemMeta()
				&& is.getItemMeta() instanceof BookMeta)	{
			BookMeta bm = (BookMeta) is.getItemMeta();
			if (bm.getTitle().equalsIgnoreCase("Captchadex")
					&& bm.getAuthor().equalsIgnoreCase(e.getPlayer().getName()))	{
				e.getPlayer().closeInventory();
				e.getPlayer().openInventory(Captchadex.loadCaptchadex(is));
				return;
			}
		}
		if (is == null || !(is.getType() == Material.PAPER)
				|| !is.hasItemMeta()
				|| !is.getItemMeta().hasDisplayName()
				|| !is.getItemMeta().getDisplayName().equals("Captchacard")
				|| !is.getItemMeta().hasLore()
				|| is.getItemMeta().getLore().contains("Blank")) {
			// Not a Captchacard
			return;
		}
		ItemStack captcha = Captcha.captchaToItem(is);
		if (is.getAmount() > 1) {
			is.setAmount(is.getAmount() - 1);
			if (e.getPlayer().getInventory().firstEmpty() != -1) {
				e.getPlayer().getInventory().addItem(captcha);
			} else {
				e.getPlayer().getWorld().dropItem(e.getPlayer().getEyeLocation(), captcha)
						.setVelocity(e.getPlayer().getLocation().getDirection().multiply(0.4));
			}
		} else {
			e.getPlayer().setItemInHand(captcha);
		}
		e.getPlayer().updateInventory();
	}

	/**
	 * The event handler for saving Captchadex <code>Inventory</code> to a
	 * <code>BookMeta</code> when closed.
	 * 
	 * @param e
	 *            the <code>InventoryCloseEvent</code>
	 */
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getInventory().getName().equals("Captchadex")) {
			Captchadex.saveCaptchadex(e.getInventory(), e.getPlayer().getItemInHand());
		}
	}

	/**
	 * The event handler for preventing <code>Player</code>s from manually
	 * creating their own Captchadexes.
	 * 
	 * @param e
	 *            the <code>PlayerEditBookEvent</code>
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBookEdit(PlayerEditBookEvent e) {
		if (e.isSigning() && e.getNewBookMeta().hasTitle() && e.getNewBookMeta().getTitle().equals("Captchadex")) {
			BookMeta bm = e.getNewBookMeta().clone();
			bm.setTitle(ChatColor.DARK_RED + "I am bad at cheating.");
			e.setNewBookMeta(bm);
			Bukkit.getServer().broadcastMessage(ChatColor.RED + e.getPlayer().getName()
					+ " just tried to title a book Captchadex. Please take a moment to laugh at them.");
		}
	}

	/**
	 * Check if a <code>Block</code> has a right click action that would take priority over opening a book.
	 * @param b the <code>Block</code> to check
	 * @return <code>true</code> if right clicking the block will not cause a book to open.
	 */
	private boolean hasRightClickFunction(Block b) {
		switch (b.getType()) {
		case BOOKSHELF:
			// Awww yiss BookShelf <3
			return Bukkit.getPluginManager().isPluginEnabled("BookShelf");
		case CAULDRON:
			// Forgot that only my plugin would cause cancellation of right clicking cauldron :V
			return Bukkit.getPluginManager().isPluginEnabled("BookSuite");
		case ANVIL:
		case BEACON:
		case BED_BLOCK:
		case BREWING_STAND:
		case BURNING_FURNACE:
		case CHEST:
		case COMMAND:
		case DAYLIGHT_DETECTOR:
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
		case DISPENSER:
		case DRAGON_EGG:
		case DROPPER:
		case ENCHANTMENT_TABLE:
		case ENDER_CHEST:
		case FENCE_GATE:
		case HOPPER:
		case ITEM_FRAME:
		case LEVER:
		case LOCKED_CHEST:
		case NOTE_BLOCK:
		case REDSTONE_COMPARATOR:
		case REDSTONE_COMPARATOR_OFF:
		case REDSTONE_COMPARATOR_ON:
		case STONE_BUTTON:
		case TRAPPED_CHEST:
		case TRAP_DOOR:
		case TRIPWIRE_HOOK:
		case WOODEN_DOOR:
		case WOOD_BUTTON:
		case WOOD_DOOR:
		case WORKBENCH:
			return true;
		default:
			return false;
		}
	}
}
