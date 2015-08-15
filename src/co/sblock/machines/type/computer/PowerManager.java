package co.sblock.machines.type.computer;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.effects.Effects;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.BehaviorReactive;
import co.sblock.effects.effect.Effect;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Computer;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.inventory.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Program for opening the Godtier power selection window.
 * 
 * @author Jikoo
 */
public class PowerManager extends Program {

	private final ItemStack icon;

	protected PowerManager() {
		icon = new ItemStack(Material.FIREWORK);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "PowerManager");
		meta.setLore(Arrays.asList(ChatColor.WHITE + "Manage Godtier powers!"));
		icon.setItemMeta(meta);

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
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		int active = 0, passive = 0;
		List<String> enabledEffects = user.getGodtierEffects();
		GodtierToggle toggle = (GodtierToggle) Programs.getProgramByName("GodtierToggle");
		for (Effect effect : Effects.getInstance().getGodtierEffects(user.getUserAspect())) {
			int slot;
			if (effect instanceof BehaviorPassive || effect instanceof BehaviorReactive) {
				if (passive > 8) {
					Machines.getInstance().getLogger().warning("Over 9 passives detected for "
							+ user.getUserAspect().getDisplayName() + ". GUI expansion is required.");
					continue;
				}
				slot = 9 + passive;
				passive++;
			} else {
				if (active > 8) {
					Machines.getInstance().getLogger().warning("Over 9 actives detected for "
							+ user.getUserAspect().getDisplayName() + ". GUI expansion is required.");
					continue;
				}
				slot = active;
				active++;
			}
			top.setItem(slot, toggle.getIcon(effect, user.getUserAspect(), enabledEffects.contains(effect.getName())));
		}
		top.setItem(22, Programs.getProgramByName("Back").getIcon());
		InventoryUtils.changeWindowName(player, "Godtier Power Manager");
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
