package co.sblock.machines.type.computer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.machines.Machines;

import net.md_5.bungee.api.ChatColor;

/**
 * A Program representing Sburb's chat client. Players must be near a Computer and have this Program
 * installed to chat in the global Channel.
 * 
 * @author Jikoo
 */
public class PesterChum extends Program {

	private final ItemStack icon;

	public PesterChum(Machines machines) {
		super(machines);
		icon = new ItemStack(Material.RAW_FISH, 1, (short) 3);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "PesterChum");
		icon.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {}

	@Override
	public boolean isDefault() {
		return true;
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
