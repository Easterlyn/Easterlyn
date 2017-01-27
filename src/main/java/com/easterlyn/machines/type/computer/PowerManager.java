package com.easterlyn.machines.type.computer;

import java.util.Arrays;
import java.util.List;

import com.easterlyn.effects.Effects;
import com.easterlyn.effects.effect.BehaviorPassive;
import com.easterlyn.effects.effect.BehaviorReactive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Computer;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.InventoryUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

/**
 * Program for opening the Godtier power selection window.
 * 
 * @author Jikoo
 */
public class PowerManager extends Program {

	private final Effects effects;
	private final Users users;
	private final ItemStack icon;
	private final ItemStack installer; // GODTIER: remove this post-testing

	public PowerManager(Machines machines) {
		super(machines);
		this.effects = machines.getPlugin().getModule(Effects.class);
		this.users = machines.getPlugin().getModule(Users.class);
		icon = new ItemStack(Material.FIREWORK);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "PowerManager");
		meta.setLore(Arrays.asList(ChatColor.WHITE + "Manage Godtier powers!"));
		icon.setItemMeta(meta);

		installer = new ItemStack(Material.RECORD_12);
		meta = installer.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "Temporary PowerManager Installer");
		installer.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		InventoryView view = player.getOpenInventory();
		if (view.getTopInventory() == null || !(view.getTopInventory().getHolder() instanceof Computer)) {
			return;
		}
		Inventory top = view.getTopInventory();
		if (top.getSize() != 27) {
			top = Bukkit.createInventory(top.getHolder(), 27, "Computer");
			player.openInventory(top);
		}
		top.clear();
		User user = users.getUser(player.getUniqueId());
		int active = 0, passive = 0;
		List<String> enabledEffects = user.getGodtierEffects();
		GodtierToggle toggle = (GodtierToggle) Programs.getProgramByName("GodtierToggle");
		for (Effect effect : effects.getGodtierEffects(user.getUserAspect())) {
			int slot;
			if (effect instanceof BehaviorPassive || effect instanceof BehaviorReactive) {
				if (passive > 8) {
					getMachines().getLogger().warning("Over 9 passives detected for "
							+ user.getUserAspect().getDisplayName() + ". GUI expansion is required.");
					continue;
				}
				slot = 9 + passive;
				passive++;
			} else {
				if (active > 8) {
					getMachines().getLogger().warning("Over 9 actives detected for "
							+ user.getUserAspect().getDisplayName() + ". GUI expansion is required.");
					continue;
				}
				slot = active;
				active++;
			}
			top.setItem(slot, toggle.getIcon(effect, user.getUserAspect(), enabledEffects.contains(effect.getName())));
		}
		top.setItem(22, Programs.getProgramByName("Back").getIcon());

		InventoryUtils.changeWindowName(player, "~/PowerManager");
	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	@Override
	public ItemStack getInstaller() {
		return installer;
	}

}
