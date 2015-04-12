package co.sblock.commands.utility;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for printing out information about a permission.
 * 
 * @author Jikoo
 */
public class PermissionInformationCommand extends SblockCommand {

	public PermissionInformationCommand() {
		super("perminfo");
		this.setDescription("Prints out information about the specified permission.");
		this.setUsage("/perminfo <permission> [player]");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Permission permission = null;
		if (args.length > 0) {
			permission = Bukkit.getPluginManager().getPermission(args[0]);
		}
		if (permission == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid permission.");
			return true;
		}
		if (args.length > 1) {
			List<Player> players = Bukkit.matchPlayer(args[1]);
			if (players.isEmpty()) {
				sender.sendMessage(ChatColor.RED + "No matching players found for " + args[1]);
			}
			sender.sendMessage(ChatColor.YELLOW + args[0] + " is "
					+ players.get(0).hasPermission(permission) + " for " + players.get(0).getName());
			return true;
		}
		sender.sendMessage(ChatColor.DARK_AQUA + "Permission: " + ChatColor.YELLOW + permission.getName());
		if (permission.getDescription() != null) {
			sender.sendMessage(ChatColor.DARK_AQUA + "Description: " + ChatColor.YELLOW + permission.getDescription());
		}
		if (permission.getChildren().size() > 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "Children:");
			for (Entry<String, Boolean> entry : permission.getChildren().entrySet()) {
				sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW)
						.append(entry.getKey()).append(ChatColor.DARK_AQUA).append(": ")
						.append(entry.getValue() ? ChatColor.GREEN : ChatColor.RED)
						.append(entry.getValue()).toString());
			}
		}
		sender.sendMessage(ChatColor.DARK_AQUA + "Default: " + ChatColor.YELLOW + permission.getDefault().name());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
