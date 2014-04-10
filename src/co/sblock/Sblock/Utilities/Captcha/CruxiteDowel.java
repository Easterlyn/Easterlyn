package co.sblock.Sblock.Utilities.Captcha;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A class for handling all functions of cruxite dowels.
 * 
 * @author Jikoo
 */
public class CruxiteDowel {

	public static ItemStack getDowel() {
		ItemStack is = new ItemStack(Material.NETHER_BRICK_ITEM);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.WHITE + "Cruxite Dowel");
		is.setItemMeta(im);
		return is;
	}

	public static boolean isBlankDowel(ItemStack is) {
		if (is != null) {
			ItemStack dowel = getDowel();
			dowel.setAmount(is.getAmount());
			return is.equals(dowel);
		}
		return false;
	}

	public static boolean isUsedDowel(ItemStack is) {
		return is != null && is.getType() == Material.NETHER_BRICK_ITEM && is.hasItemMeta()
				&& is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()
				&& is.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Cruxite Dowel");
	}

	public static ItemStack carve(ItemStack is) {
		ItemStack dowel = getDowel();
		ItemMeta im = dowel.getItemMeta();
		im.setLore(is.getItemMeta().getLore());
		dowel.setItemMeta(im);
		return dowel;
	}
}
