package co.sblock.machines.type.computer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Computer;
import co.sblock.utilities.InventoryUtils;

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
		InventoryView view = player.getOpenInventory();
		if (view.getTopInventory() == null || !(view.getTopInventory().getHolder() instanceof Computer)) {
			return;
		}
		Inventory top = view.getTopInventory();
		if (top.getSize() > 9) {
			// Open a fresh default 9-slot inventory.
			top = top.getHolder().getInventory();
			player.openInventory(top);
		} else {
			top.clear();
		}

		MachinePurchase purchase = (MachinePurchase) Programs.getProgramByName("MachinePurchase");
		top.setItem(0, purchase.getIconForMachine(getMachines().getMachineByName("Cruxtruder")));
		top.setItem(1, purchase.getIconForMachine(getMachines().getMachineByName("PunchDesignix")));
		top.setItem(2, purchase.getIconForMachine(getMachines().getMachineByName("TotemLathe")));
		top.setItem(3, purchase.getIconForMachine(getMachines().getMachineByName("Alchemiter")));
		top.setItem(4, purchase.getIconForMachine(getMachines().getMachineByName("Transportalizer")));
		top.setItem(5, purchase.getIconForMachine(getMachines().getMachineByName("Elevator")));

		top.setItem(8, Programs.getProgramByName("Back").getIcon());
		InventoryUtils.changeWindowName(player, "Machine Client");
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
