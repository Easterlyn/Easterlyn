/**
 * 
 */
package co.sblock.Sblock.Machines.Type;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Jikoo
 *
 */
public enum Icon {
	PESTERCHUM(Material.GOLD_RECORD, "Pesterchum", 1),
	SBURBBETACLIENT(Material.RECORD_5, "Sburb Beta Client", 5),
	SBURBBETASERVER(Material.GREEN_RECORD, "Sburb Beta Server", 2),
	SBURBALPHACLIENT(Material.RECORD_4, "Sburb Alpha Client", 4),
	SBURBALPHASERVER(Material.RECORD_3, "Sburb Alpha Server", 3),
	SGRUB(Material.RECORD_6, "Sgrub", 2);
	// GRISTTORRENT ahaaaano.

	private Material m;
	private String name;
	private int number;

	Icon(Material m, String name, int number) {
		this.m = m;
		this.name = name;
		this.number = number;
	}

	public ItemStack getIcon() {
		ItemStack is = new ItemStack(m);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		return is;
	}

	public static Icon getIcon(Material m) {
		for (Icon i : Icon.values()) {
			if (i.m.equals(m)) {
				return i;
			}
		}
		return null;
	}

	public static Icon getIcon(int i1) {
		for (Icon i : Icon.values()) {
			if (i.number == i1) {
				return i;
			}
		}
		return null;
	}

	public void execute(Player p) {
		// TODO computer fancy functions
		p.sendMessage(this.name() + ".~ATH EXECUTING.");
	}
}
