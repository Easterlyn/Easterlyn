package co.sblock.Sblock.Utilities.Captcha;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Captchadex {

	public static ItemStack createCaptchadexBook(Player p) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bm = (BookMeta) book.getItemMeta();
		bm.setTitle("Captchadex");
		bm.setAuthor(p.getName());
		bm.setPage(1, "\n\n\n\n\nThis page intentionally         left blank");
		book.setItemMeta(bm);
		return book;
	}

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

	public static ItemStack saveCaptchadex(Inventory i, ItemStack book) {
		BookMeta bm = (BookMeta) book.getItemMeta();
		ItemStack[] contents = i.getContents();
		bm.setPage(1, "\n\n\n\n\nThis page intentionally         left blank");
		for (int slot = 0; slot < contents.length; slot++) {
			ItemStack is = contents[slot];
			if (is != null) {
				ItemStack captcha = Captcha.itemToCaptcha(is);
				StringBuilder save = new StringBuilder();
				List<String> lore = captcha.getItemMeta().getLore();
				for (String s : lore) {
					save.append(s).append("\n");
				}
				bm.setPage(slot + 2, save.toString());
			}
		}
		book.setItemMeta(bm);
		return book;
	}

	public static Inventory createCaptchadexInventory(InventoryHolder ih) {
		return Bukkit.getServer().createInventory(ih, 27, "Captchadex");
	}

	public static ItemStack punchCardToItem(ItemStack is) {
		ItemStack pc = Captcha.captchaToItem(is);
		return pc;
	}

	public static ItemStack itemToCard(ItemStack is) {
		ItemStack pc = Captcha.itemToCaptcha(is);
		ItemMeta im = pc.getItemMeta();
		im.setDisplayName("Punchcard");
		pc.setItemMeta(im);
		return pc;
	}

	public static ItemStack punchCard(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Punchcard");
		is.setItemMeta(im);
		return is;
	}

}
