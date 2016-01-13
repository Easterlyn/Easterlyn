package co.sblock.machines.type.computer;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Computer;

/**
 * Representation for a back button.
 * 
 * @author Jikoo
 */
public class Back extends Program {

	private final ItemStack icon;

	public Back(Machines machines) {
		super(machines);
		icon = new ItemStack(Material.REDSTONE_BLOCK);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "Back");
		meta.setLore(Arrays.asList(ChatColor.WHITE + "cd ~/"));
		icon.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
			List<String> lines = clicked.getItemMeta().getLore();
			if (!lines.isEmpty()) {
				String line = lines.get(0);
				if (line.length() > 7) {
					Program program = Programs.getProgramByName(line.substring(7));
					if (program != null) {
						program.execute(player, clicked);
						return;
					}
				}
			}
		}
		((Computer) getMachines().getMachineByName("Computer")).openInventory(player);
	}

	public ItemStack getBackTo(String program) {
		ItemStack icon = this.icon.clone();
		ItemMeta meta = icon.getItemMeta();
		meta.setLore(Arrays.asList(ChatColor.WHITE + "cd ~/" + program));
		icon.setItemMeta(meta);
		return icon;
	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	@Override
	public ItemStack getInstaller() {
		return null;
	}

}
