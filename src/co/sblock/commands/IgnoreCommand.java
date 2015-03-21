package co.sblock.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * SblockCommand for (un)ignoring a Player.
 * 
 * @author Jikoo
 */
public class IgnoreCommand extends SblockAsynchronousCommand {

	public IgnoreCommand() {
		super("ignore");
		setDescription("Ignore someone.");
		setUsage("/(un)ignore <player>");
		setAliases("unignore");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		UUID uuid = getUniqueId(args[0]);
		if (uuid == null) {
			sender.sendMessage(ChatColor.RED + "Unknown player!");
			return true;
		}
		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());

		if (label.equalsIgnoreCase("ignore")) {
			if (user.addIgnore(uuid)) {
				sender.sendMessage(ChatColor.GREEN + "Now ignoring " + Bukkit.getOfflinePlayer(uuid).getName() + "!");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Already ignoring " + Bukkit.getOfflinePlayer(uuid).getName() + "!");
			}
		} else {
			if (user.removeIgnore(uuid)) {
				sender.sendMessage(ChatColor.GREEN + "No longer ignoring " + Bukkit.getOfflinePlayer(uuid).getName() + "!");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Weren't ignoring " + Bukkit.getOfflinePlayer(uuid).getName() + "!");
			}
		}
		return true;
	}

}
