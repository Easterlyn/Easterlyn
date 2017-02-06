package com.easterlyn.commands.info;

import java.util.List;
import java.util.Map.Entry;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

/**
 * EasterlynCommand for printing out information about a permission.
 * 
 * @author Jikoo
 */
public class PermissionInformationCommand extends EasterlynCommand {

	public PermissionInformationCommand(Easterlyn plugin) {
		super(plugin, "perminfo");
		this.setDescription("Prints out information about the specified permission.");
		this.setPermissionLevel(UserRank.FELT);
		this.setUsage("/perminfo <permission> [player]");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		if (args.length > 1) {
			if (args[1].equals("-find")) {
				for (Permission perm : getPlugin().getServer().getPluginManager().getPermissions()) {
					if (perm.getName().equals(args[0]) || perm.getChildren().keySet().contains(args[0])) {
						sender.sendMessage(args[0] + " is influenced by " + perm.getName());
					}
				}
				sender.sendMessage("Permission search complete.");
				return true;
			}
			List<Player> players = Bukkit.matchPlayer(args[1]);
			if (players.isEmpty()) {
				sender.sendMessage(Language.getColor("bad") + "No matching players found for " + args[1]);
				return true;
			}
			sender.sendMessage(Language.getColor("neutral") + args[0] + " is "
					+ players.get(0).hasPermission(args[0]) + " for " + players.get(0).getName());
			return true;
		}
		Permission permission = Bukkit.getPluginManager().getPermission(args[0]);
		if (permission == null) {
			sender.sendMessage(Language.getColor("bad") + args[0] + " is not a valid permission.");
			return true;
		}
		sender.sendMessage(Language.getColor("emphasis.neutral") + "Permission: " + Language.getColor("neutral") + permission.getName());
		if (permission.getDescription() != null) {
			sender.sendMessage(Language.getColor("emphasis.neutral") + "Description: " + Language.getColor("neutral") + permission.getDescription());
		}
		if (permission.getChildren().size() > 0) {
			sender.sendMessage(Language.getColor("emphasis.neutral") + "Children:");
			for (Entry<String, Boolean> entry : permission.getChildren().entrySet()) {
				sender.sendMessage(new StringBuilder().append(Language.getColor("neutral"))
						.append(entry.getKey()).append(Language.getColor("emphasis.neutral")).append(": ")
						.append(entry.getValue() ? Language.getColor("good") : Language.getColor("bad"))
						.append(entry.getValue()).toString());
			}
		}
		sender.sendMessage(Language.getColor("emphasis.neutral") + "Default: " + Language.getColor("neutral") + permission.getDefault().name());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
