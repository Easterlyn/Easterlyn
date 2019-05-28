package com.easterlyn.machines.type.computer;

import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Computer;
import com.easterlyn.utilities.InventoryUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

/**
 * Program for opening a verification menu.
 * 
 * @author Jikoo
 */
public class Verification extends Program {

	private final String lore = ChatColor.WHITE + "sudo %s";
	private final ItemStack icon;

	public Verification(Machines machines) {
		super(machines);
		icon = new ItemStack(Material.EMERALD_BLOCK);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Confirm");
		icon.setItemMeta(meta);
	}

	public void openInventory(Player player, String verification) {
		InventoryView view = player.getOpenInventory();
		if (view.getTopInventory() == null || !(view.getTopInventory().getHolder() instanceof Computer)) {
			return;
		}
		Inventory top = view.getTopInventory();
		if (top.getSize() > 9) {
			// Open a fresh default 9-slot inventory.
			top = top.getHolder().getInventory();
			player.openInventory(top);
		}
		top.clear();
		ItemStack icon = this.icon.clone();
		ItemMeta meta = icon.getItemMeta();
		meta.setLore(Collections.singletonList(String.format(lore, verification)));
		icon.setItemMeta(meta);
		top.setItem(0, icon);
		top.setItem(8, Programs.getProgramByName("Back").getIcon());
		if (verification.length() > 12) {
			verification = verification.substring(0, 12);
		}
		String title = "~/Verify?initialize=%s";
		InventoryUtils.changeWindowName(player, String.format(title, verification));
	}

	@Override
	public void execute(Player player, ItemStack clicked) {
		if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasLore()) {
			return;
		}
		Program toOpen = Programs.getProgramByName(clicked.getItemMeta().getLore().get(0).replaceFirst("..sudo ", ""));
		if (toOpen != null) {
			toOpen.execute(player, clicked, true);
		}
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

}
