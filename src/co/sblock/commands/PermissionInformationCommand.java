package co.sblock.commands;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import com.google.common.collect.ImmutableList;

/**
 * SblockCommand for printing out information about a permission.
 * 
 * @author Jikoo
 */
public class PermissionInformationCommand extends SblockCommand {

	public PermissionInformationCommand() {
		super("perminfo");
		this.setDescription("Prints out information about the specified permission.");
		this.setUsage("/perminfo <permission>");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Permission permission = null;
		if (args.length > 0) {
			permission = Bukkit.getPluginManager().getPermission(args[0]);
		}
		if (permission == null) {
			sender.sendMessage(ChatColor.RED + "Please enter a valid permission.");
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
		if (!sender.hasPermission(this.getPermission()) || args.length > 0) {
			return ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}
}
