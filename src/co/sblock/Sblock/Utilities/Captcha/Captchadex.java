package co.sblock.Sblock.Utilities.Captcha;

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
	 * Create an <code>ItemStack</code> representation of a
	 * <code>Captchadex</code> for a <code>Player</code>.
	 * 
	 * @param p
	 *            the <code>Player</code>
	 * @return the <code>ItemStack</code> created
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
	 * Translate <code>Captchadex</code> contents from an <code>ItemStack</code>
	 * into an <code>Inventory</code>.
	 * 
	 * @param book
	 *            the <code>ItemStack</code> to translate
	 * @return the resulting <code>Inventory</code>
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
	 * Translate Inventory contents into an <code>ItemStack</code>.
	 * 
	 * @param i
	 *            the <code>Inventory</code> to save
	 * @param book
	 *            the <code>ItemStack</code> to save into
	 * @return the saved <code>ItemStack</code>
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
	 * Creates a <code>Captchadex</code> Inventory for the specified
	 * <code>InventoryHolder</code>.
	 * 
	 * @param ih
	 *            <code>InventoryHolder</code>
	 * @return the created <code>Inventory</code>
	 */
	public static Inventory createCaptchadexInventory(InventoryHolder ih) {
		return Bukkit.getServer().createInventory(ih, 27, "Captchadex");
	}

	/**
	 * Creat a captchacard of an <code>ItemStack</code>.
	 * 
	 * @param is
	 *            the <code>ItemStack</code>
	 * @return the captchacard created
	 */
	public static ItemStack punchCardToItem(ItemStack is) {
		ItemStack pc = Captcha.captchaToItem(is);
		return pc;
	}

	/**
	 * Create a captchacard from a punchcard.
	 * 
	 * @param is
	 *            the captchacard to punch
	 * @return the punched captchacard
	 */
	public static ItemStack itemToCard(ItemStack is) {
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
	 * @param is
	 *            the punchcard to unpunch
	 * @return the unpunched captchacard
	 */
	public static ItemStack punchCard(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Punchcard");
		is.setItemMeta(im);
		return is;
	}
}
