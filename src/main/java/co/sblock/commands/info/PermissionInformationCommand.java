package co.sblock.commands.info;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for printing out information about a permission.
 * 
 * @author Jikoo
 */
public class PermissionInformationCommand extends SblockCommand {

	public PermissionInformationCommand(Sblock plugin) {
		super(plugin, "perminfo");
		this.setDescription("Prints out information about the specified permission.");
		this.setUsage("/perminfo <permission> [player]");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		Permission permission = Bukkit.getPluginManager().getPermission(args[0]);
		if (permission == null) {
			sender.sendMessage(Language.getColor("bad") + args[0] + " is not a valid permission.");
			return true;
		}
		if (args.length > 1) {
			List<Player> players = Bukkit.matchPlayer(args[1]);
			if (players.isEmpty()) {
				sender.sendMessage(Language.getColor("bad") + "No matching players found for " + args[1]);
			}
			sender.sendMessage(Language.getColor("neutral") + args[0] + " is "
					+ players.get(0).hasPermission(permission) + " for " + players.get(0).getName());
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
