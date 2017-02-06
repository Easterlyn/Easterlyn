package com.easterlyn.machines.type.computer;

import java.util.Arrays;

import com.easterlyn.chat.Language;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.utilities.Experience;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Program for purchasing a Machine.
 * 
 * @author Jikoo
 */
public class MachinePurchase extends Program {

	private final ItemStack icon;

	public MachinePurchase(Machines machines) {
		super(machines);
		icon = new ItemStack(Material.DIRT);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(Language.getColor("good") + "Purchase");
		icon.setItemMeta(meta);
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
		Machine machine = getMachines().getMachineByName(meta.getLore().get(0));
		if (machine == null) {
			return;
		}
		if (Experience.getExp(player) < machine.getCost()) {
			player.sendMessage(Language.getColor("bad") + "You don't have enough mana!");
			return;
		}
		Experience.changeExp(player, - machine.getCost());
		player.getWorld().dropItem(player.getLocation(), machine.getUniqueDrop()).setPickupDelay(0);
	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	public ItemStack getIconForMachine(Machine machine) {
		if (machine == null || machine.getUniqueDrop() == null) {
			return null;
		}
		ItemStack item = machine.getUniqueDrop().clone();
		ItemMeta meta = item.getItemMeta();
		meta.setLore(Arrays.asList(meta.getDisplayName(),
				Language.getColor("emphasis.neutral").toString() + machine.getCost() + Language.getColor("neutral") + " mana"));
		meta.setDisplayName(Language.getColor("good") + "Purchase");
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public ItemStack getInstaller() {
		return null;
	}

}
