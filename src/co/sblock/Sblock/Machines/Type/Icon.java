package co.sblock.Sblock.Machines.Type;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Jikoo
 */
public enum Icon {

	PESTERCHUM(Material.GOLD_RECORD, ChatColor.WHITE + "Pesterchum", 1, null),
	SBURBBETACLIENT(Material.RECORD_5, ChatColor.WHITE + "SburbBeta", 5, null),
	SBURBBETASERVER(Material.GREEN_RECORD, ChatColor.WHITE + "SburbServer", 2, null),
//	SBURBALPHACLIENT(Material.RECORD_4, "Sburb Alpha Client", 4, null),
//	SBURBALPHASERVER(Material.RECORD_3, "Sburb Alpha Server", 3, null),
//	SGRUB(Material.RECORD_6, "Sgrub", 6, null)
	// GRISTTORRENT ahaaaano.
	BACK(Material.REDSTONE_BLOCK, ChatColor.DARK_RED + "Back", 0, "cd ..");

	/** The <code>Material</code> of the program. */
	private Material m;
	/** The name of the program. */
	private String name;
	/** The program ID. */
	private int number;
	/** Additional lore to add to the program. */
	private String lore;

	private Icon(Material m, String name, int number, String lore) {
		this.m = m;
		this.name = name;
		this.number = number;
		this.lore = lore;
	}

	/**
	 * Gets the program's identifying ItemStack, the ingame "icon."
	 * 
	 * @return ItemStack
	 */
	public ItemStack getIcon() {
		ItemStack is = new ItemStack(m);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		if (lore != null) {
			ArrayList<String> loreList = new ArrayList<String>();
			loreList.add(lore);
			im.setLore(loreList);
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
	 * @see java.lang.Enum#toString()
	 */
	public String toString() {
		return this.name;
	}

	/**
	 * Get an <code>Icon</code> by <code>Material</code>.
	 * 
	 * @param m
	 *            the <code>Material</code> to match
	 * @return the <code>Icon</code>
	 */
	public static Icon getIcon(Material m) {
		for (Icon i : Icon.values()) {
			if (i.m.equals(m)) {
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
