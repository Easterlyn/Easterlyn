package co.sblock.utilities.captcha;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Dublek, Jikoo
 */
public class Captchadex {

	/**
	 * Create an ItemStack representation of a Captchadex.
	 * 
	 * @param p the Player who owns the book
	 * 
	 * @return the ItemStack created
	 */
	public static ItemStack createCaptchadexBook(Player p) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bm = (BookMeta) book.getItemMeta();
		bm.setTitle("Captchadex");
		bm.setAuthor(p.getName());
		bm.addPage("\n\n\n\n\nThis page intentionally         left blank");
		book.setItemMeta(bm);
		return book;
	}

	/**
	 * Translate Captchadex contents into an Inventory.
	 * 
	 * @param book the ItemStack to translate
	 * 
	 * @return the resulting Inventory
	 */
	public static Inventory loadCaptchadex(ItemStack book) {
		BookMeta bm = (BookMeta) book.getItemMeta();
		Player p = Bukkit.getPlayer(bm.getAuthor());
		Inventory i = createCaptchadexInventory(p);
		if (bm.getPageCount() > 1) {
			for (int i1 = 2; i1 <= bm.getPageCount(); i1++) {
				String[] data = bm.getPage(i1).split("\n");
				if (data.length > 1) {
					// Page not a null ItemStack
					i.setItem(i1 - 2, Captcha.getCaptchaItem(data));
				}
			}
		}
		return i;
	}

	/**
	 * Translate Inventory contents into an ItemStack.
	 * 
	 * @param i the Inventory to save
	 * @param book the ItemStack to save into
	 * 
	 * @return the saved ItemStack
	 */
	public static ItemStack saveCaptchadex(Inventory i, ItemStack book) {
		BookMeta bm = (BookMeta) book.getItemMeta();
		ItemStack[] contents = i.getContents();
		bm.setPages(new ArrayList<String>());
		bm.addPage("\n\n\n\n\nThis page intentionally         left blank");
		for (int slot = 0; slot < contents.length; slot++) {
			ItemStack is = contents[slot];
			if (is != null) {
				ItemStack captcha = Captcha.itemToCaptcha(is);
				StringBuilder save = new StringBuilder();
				List<String> lore = captcha.getItemMeta().getLore();
				for (String s : lore) {
					save.append(s).append('\n');
				}
				bm.addPage(save.toString());
			}
		}
		book.setItemMeta(bm);
		return book;
	}

	/**
	 * Check to see if the ItemStack in a player's hand is a Captchadex.
	 * 
	 * @param p the Player
	 * 
	 * @return true if the item in the Player's hand is a Captchadex
	 */
	public static boolean isCaptchadex(Player p) {
		ItemStack is = p.getItemInHand();
		if (is != null && is.getType().equals(Material.WRITTEN_BOOK) && is.hasItemMeta()
				&& is.getItemMeta() instanceof BookMeta) {
			BookMeta bm = (BookMeta) is.getItemMeta();
			return bm.getTitle().equalsIgnoreCase("Captchadex")
					&& bm.getAuthor().equalsIgnoreCase(p.getName());
		}
		return false;
	}

	/**
	 * Creates a Captchadex Inventory for the specified InventoryHolder.
	 * 
	 * @param ih the InventoryHolder
	 * 
	 * @return the Inventory created
	 */
	public static Inventory createCaptchadexInventory(InventoryHolder ih) {
		return Bukkit.getServer().createInventory(ih, 27, "Captchadex");
	}

	/**
	 * Creates a captchacard from an ItemStack.
	 * 
	 * @param is the ItemStack
	 * 
	 * @return the captchacard created
	 */
	public static ItemStack punchcardToItem(ItemStack is) {
		return Captcha.captchaToItem(is);
	}

	/**
	 * Create a punchcard from an ItemStack.
	 * 
	 * @param is the ItemStack
	 * 
	 * @return the punched captchacard
	 */
	public static ItemStack itemToPunchcard(ItemStack is) {
		ItemStack pc = Captcha.itemToCaptcha(is);
		ItemMeta im = pc.getItemMeta();
		im.setDisplayName("Punchcard");
		pc.setItemMeta(im);
		return pc;
	}

	/**
	 * Create a punchcard from a captchacard.
	 * <p>
	 * For testing purposes only, good luck patching punched holes.
	 * 
	 * @param is the punchcard ItemStack
	 * 
	 * @return the unpunched captchacard
	 */
	public static ItemStack punchCard(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Punchcard");
		is.setItemMeta(im);
		return is;
	}
}
