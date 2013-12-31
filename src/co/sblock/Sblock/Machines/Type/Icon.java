package co.sblock.Sblock.Machines.Type;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Enum for supplying <code>ItemStack</code>s that represent
 * "programs" installed on a user's <code>Computer</code>.
 * 
 * @author Jikoo
 */
public enum Icon {

	PESTERCHUM(1), SBURBCLIENT(5), SBURBSERVER(2),
	BACK(0), CONFIRM(0);

	/** The program ID. */
	private int number;

	private Icon(int number) {
		this.number = number;
	}

	/**
	 * Gets the "program installer" <code>ItemStack</code>, if any.
	 * 
	 * @return is the installer <code>ItemStack</code>
	 */
	public ItemStack getInstaller() {
		ItemStack is = new ItemStack(Material.GOLD_RECORD);
		ItemMeta im = is.getItemMeta();
		switch (this) {
		case PESTERCHUM:
			is.setType(Material.GOLD_RECORD);
			im.setDisplayName(ChatColor.YELLOW + "Pesterchum");
			break;
		case SBURBCLIENT:
			is.setType(Material.RECORD_5);
			im.setDisplayName(ChatColor.GREEN + "SburbClient");
			break;
		case SBURBSERVER:
			is.setType(Material.GREEN_RECORD);
			im.setDisplayName(ChatColor.GREEN + "SburbServer");
			break;
		default:
			return null;
		}
		is.setItemMeta(im);
		return is;
	}

	/**
	 * Gets the program's identifying ItemStack, the ingame "icon."
	 * 
	 * @return ItemStack
	 */
	public ItemStack getIcon() {
		ItemStack is = new ItemStack(Material.DIRT);
		ItemMeta im = is.getItemMeta();
		switch(this) {
		case BACK:
			is.setType(Material.REDSTONE_BLOCK);
			im.setDisplayName(ChatColor.DARK_RED + "Back");
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.WHITE + "cd ..");
			im.setLore(lore);
			break;
		case CONFIRM:
			is.setType(Material.EMERALD_BLOCK);
			im.setDisplayName(ChatColor.GREEN + "Confirm");
		case PESTERCHUM:
			is.setType(Material.RAW_FISH);
			is.setDurability((short) 3);
			im.setDisplayName(ChatColor.YELLOW + "Pesterchum");
			break;
		case SBURBCLIENT:
			is.setType(Material.WORKBENCH);
			im.setDisplayName(ChatColor.GREEN + "SburbClient");
			break;
		case SBURBSERVER:
			is.setType(Material.ENDER_PORTAL_FRAME);
			im.setDisplayName(ChatColor.GREEN + "SburbServer");
			break;
		default:
			break;
		}
		is.setItemMeta(im);
		return is;
	}

	/**
	 * Gets the program ID.
	 * 
	 * @return <code>int</code>
	 */
	public int getProgramID() {
		return this.number;
	}

	/**
	 * Get an <code>Icon</code> by "installer" <code>ItemStack</code>.
	 * 
	 * @param m
	 *            the <code>Material</code> to match
	 * @return the <code>Icon</code>
	 */
	public static Icon getIcon(ItemStack is) {
		for (Icon i : Icon.values()) {
			ItemStack installer = i.getInstaller();
			if (installer != null && installer.equals(is)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Get an <code>Icon</code> by number.
	 * 
	 * @param i1
	 *            the number to match
	 * @return the <code>Icon</code>
	 */
	public static Icon getIcon(int i1) {
		for (Icon i : Icon.values()) {
			if (i.number == i1) {
				return i;
			}
		}
		return null;
	}
}
