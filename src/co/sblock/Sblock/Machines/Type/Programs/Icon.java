package co.sblock.Sblock.Machines.Type.Programs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Jikoo
 */
public enum Icon {

	PESTERCHUM(Material.GOLD_RECORD, "Pesterchum", 1),
//	SBURBBETACLIENT(Material.RECORD_5, "Sburb Beta Client", 5),
	SBURBBETASERVER(Material.GREEN_RECORD, "Sburb Beta Server", 2),
//	SBURBALPHACLIENT(Material.RECORD_4, "Sburb Alpha Client", 4),
//	SBURBALPHASERVER(Material.RECORD_3, "Sburb Alpha Server", 3),
	SGRUB(Material.RECORD_6, "Sgrub", 6);
	// GRISTTORRENT ahaaaano.

	/** The <code>Material</code> of the program. */
	private Material m;
	/** The name of the program. */
	private String name;
	/** The number of the program. */
	private int number;

	private Icon(Material m, String name, int number) {
		this.m = m;
		this.name = name;
		this.number = number;
	}

	/**
	 * Method getIcon.
	 * @return ItemStack
	 */
	public ItemStack getIcon() {
		ItemStack is = new ItemStack(m);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		return is;
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

	/**
	 * Execute the "program" represented by the <code>Icon</code>.
	 * 
	 * @param p
	 *            the <code>Player</code> involved
	 */
	public void execute(Player p) {
		// Adam computer fancy functions
		p.sendMessage(this.name() + ".~ATH EXECUTING.");
	}
}
