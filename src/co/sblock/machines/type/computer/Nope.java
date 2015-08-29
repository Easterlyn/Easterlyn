package co.sblock.machines.type.computer;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

/**
 * Icon for indicating an element is present, just unusable.
 * 
 * @author Jikoo
 */
public class Nope extends Program {

	private final ItemStack icon;

	public Nope() {
		icon = new ItemStack(Material.BARRIER);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Currently Unavailable");
		icon.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	@Override
	public ItemStack getInstaller() {
		return null;
	}

	public ItemStack getIconFor(String... lore) {
		ItemStack icon = this.icon.clone();
		ItemMeta meta = icon.getItemMeta();
		meta.setLore(Arrays.asList(lore));
		icon.setItemMeta(meta);
		return icon;
	}

}
