package co.sblock.machines.type.computer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.machines.Machines;

import net.md_5.bungee.api.ChatColor;

/**
 * A Program allowing a Player to obtain Machines provided they have passed a certain
 * ProgressionState.
 * 
 * @author Jikoo
 */
public class MachineClient extends Program {

	private final ItemStack icon;

	public MachineClient(Machines machines) {
		super(machines);
		icon = new ItemStack(Material.WORKBENCH);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "MachineClient");
		icon.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		// ENTRY: if entry complete, machines buyable/spawnable here
		
	}

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
