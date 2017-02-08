package com.easterlyn.commands.cheat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

/**
 * EasterlynCommand for repairing an item.
 * 
 * @author Jikoo
 */
public class RepairCommand extends EasterlynCommand {

	public RepairCommand(Easterlyn plugin) {
		super(plugin, "repair");
		this.setPermissionLevel(UserRank.ADMIN);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null || hand.getType() == Material.AIR) {
			return false;
		}
		if (hand.getType().getMaxDurability() > 0) {
			hand.setDurability((short) 0);
		}
		if (args.length > 0 && hand.hasItemMeta() && args[0].equalsIgnoreCase("full")) {
			ItemMeta meta = hand.getItemMeta();
			Repairable repairable = (Repairable) meta;
			repairable.setRepairCost(0);
			hand.setItemMeta(meta);
		}
		player.getInventory().setItemInMainHand(hand);
		player.sendMessage(getLang().getValue("command.repair.success"));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (!sender.hasPermission(this.getPermission()) || args.length != 1) {
			return ImmutableList.of();
		}
		return ImmutableList.of("full");
	}

}
