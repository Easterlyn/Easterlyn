package co.sblock.machines.type.computer;

import java.util.Arrays;

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

	protected Back() {
		icon = new ItemStack(Material.REDSTONE_BLOCK);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "Back");
		meta.setLore(Arrays.asList(ChatColor.WHITE + "cd ~/"));
		icon.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		((Computer) Machines.getMachineByName("Computer")).openInventory(player);
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
