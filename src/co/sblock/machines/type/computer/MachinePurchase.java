package co.sblock.machines.type.computer;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.Color;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.utilities.Experience;

/**
 * Program for purchasing a Machine.
 * 
 * @author Jikoo
 */
public class MachinePurchase extends Program {

	public MachinePurchase(Machines machines) {
		super(machines);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		if (!clicked.hasItemMeta()) {
			return;
		}
		// TODO possibly verify?
		ItemMeta meta = clicked.getItemMeta();
		if (!meta.hasLore()) {
			return;
		}
		Machine machine = getMachines().getMachineByName(meta.getLore().get(0).substring(2));
		if (machine == null) {
			return;
		}
		Experience.changeExp(player, - machine.getCost());
		player.getWorld().dropItem(player.getLocation(), machine.getUniqueDrop()).setPickupDelay(0);
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.DIRT);
	}

	public ItemStack getIconForMachine(Machine machine) {
		ItemStack item = machine.getUniqueDrop().clone();
		ItemMeta meta = item.getItemMeta();
		meta.setLore(Arrays.asList(meta.getDisplayName(),
				Color.GOOD_EMPHASIS.toString() + machine.getCost() + Color.GOOD + " grist"));
		meta.setDisplayName(Color.GOOD + "Purchase");
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public ItemStack getInstaller() {
		return null;
	}

}
