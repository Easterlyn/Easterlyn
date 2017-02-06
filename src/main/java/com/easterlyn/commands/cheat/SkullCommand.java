package com.easterlyn.commands.cheat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommandAlias;
import com.easterlyn.users.UserRank;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * EasterlynCommandAlias for spawning or changing a skull to a particular owner.
 * 
 * @author Jikoo
 */
public class SkullCommand extends EasterlynCommandAlias {

	public SkullCommand(Easterlyn plugin) {
		super(plugin, "skull", "lore");
		this.setAliases("head");
		this.setPermissionLevel(UserRank.FELT);
		this.setUsage("/skull <player>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}

		if (this.getPermission() != null && !sender.hasPermission(this.getPermission())) {
			sender.sendMessage(this.getPermissionMessage());
			return true;
		}

		if (args.length == 0) {
			return false;
		}

		String[] newArgs = new String[2];
		newArgs[0] = "owner";
		newArgs[1] = args[0];
		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null || hand.getType() != Material.SKULL_ITEM) {
			player.getInventory().setItemInMainHand(new ItemStack(Material.SKULL_ITEM, 1, (short) 3));
			if (hand != null && hand.getType() != Material.AIR) {
				player.getLocation().getWorld().dropItem(player.getLocation(), hand).setPickupDelay(0);
			}
		}
		getCommand().execute(sender, label, newArgs);
		// This being a EasterlynCommand, it will handle its own usage when execute fails.
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (args.length == 1) {
			return super.tabComplete(sender, alias, args);
		}
		return com.google.common.collect.ImmutableList.of();
	}

}
