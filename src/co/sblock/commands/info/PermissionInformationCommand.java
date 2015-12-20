package co.sblock.commands.info;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
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
			sender.sendMessage(Color.BAD + args[0] + " is not a valid permission.");
			return true;
		}
		if (args.length > 1) {
			List<Player> players = Bukkit.matchPlayer(args[1]);
			if (players.isEmpty()) {
				sender.sendMessage(Color.BAD + "No matching players found for " + args[1]);
			}
			sender.sendMessage(Color.GOOD + args[0] + " is "
					+ players.get(0).hasPermission(permission) + " for " + players.get(0).getName());
			return true;
		}
		sender.sendMessage(Color.GOOD + "Permission: " + Color.GOOD_EMPHASIS + permission.getName());
		if (permission.getDescription() != null) {
			sender.sendMessage(Color.GOOD + "Description: " + Color.GOOD_EMPHASIS + permission.getDescription());
		}
		if (permission.getChildren().size() > 0) {
			sender.sendMessage(Color.GOOD + "Children:");
			for (Entry<String, Boolean> entry : permission.getChildren().entrySet()) {
				sender.sendMessage(new StringBuilder().append(Color.GOOD_EMPHASIS)
						.append(entry.getKey()).append(Color.GOOD).append(": ")
						.append(entry.getValue() ? Color.GOOD : Color.BAD)
						.append(entry.getValue()).toString());
			}
		}
		sender.sendMessage(Color.GOOD + "Default: " + Color.GOOD_EMPHASIS + permission.getDefault().name());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
