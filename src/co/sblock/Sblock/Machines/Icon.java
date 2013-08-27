/**
 * 
 */
package co.sblock.Sblock.Machines;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Jikoo
 *
 */
public enum Icon {
	PESTERCHUM(Material.GOLD_RECORD, "Pesterchum"),
	SBURBBETACLIENT(Material.RECORD_5, "Sburb Beta Client"),
	SBURBBETASERVER(Material.GREEN_RECORD, "Sburb Beta Server"),
	SBURBALPHACLIENT(Material.RECORD_4, "Sburb Alpha Client"),
	SBURBALPHASERVER(Material.RECORD_3, "Sburb Alpha Server"),
	SGRUB(Material.RECORD_6, "Sgrub");

	private Material m;
	private String name;

	Icon(Material m, String name) {
		this.m = m;
		this.name = name;
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

	public void execute(Player p) {
		// TODO computer fancy functions
		p.sendMessage(this.name() + ".~ATH EXECUTING.");
	}
}
