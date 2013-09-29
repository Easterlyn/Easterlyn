package co.sblock.Sblock.Utilities.Captcha;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Captchadex	{
	
	@SuppressWarnings("unused")
	private HumanEntity player;
	@SuppressWarnings("unused")
	private BookMeta bm;
	
	public Captchadex(HumanEntity p, BookMeta bm)	{
		player = p;
		this.bm = bm;
	}
	
	public static Inventory createCaptchadex(InventoryHolder ih)	{
		return Bukkit.getServer().createInventory(ih, 27, "Captchadex");
	}

	public static ItemStack punchCardToItem(ItemStack is)	{
		ItemStack pc = Captcha.captchaToItem(is);
		return pc;
	}
	public static ItemStack itemToCard(ItemStack is)	{
		ItemStack pc = Captcha.itemToCaptcha(is);
		ItemMeta im = pc.getItemMeta();
		im.setDisplayName("Punchcard");
		pc.setItemMeta(im);
		return pc;
	}
	public static ItemStack punchCard(ItemStack is)	{
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Punchcard");
		is.setItemMeta(im);
		return is;
	}

}
