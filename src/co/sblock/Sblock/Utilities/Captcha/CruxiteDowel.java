package co.sblock.Sblock.Utilities.Captcha;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Jikoo
 *
 */
public class CruxiteDowel {

	public static ItemStack getDowel() {
		ItemStack is = new ItemStack(Material.NETHER_BRICK_ITEM);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.WHITE + "Cruxite Dowel");
		is.setItemMeta(im);
		return is;
	}
	
	public static boolean isDowel(ItemStack is)	{
		return false;
	}
}
