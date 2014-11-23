package co.sblock.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * SblockCommand for checking a User's classpect.
 * 
 * @author Jikoo
 */
public class ProfileCommand extends SblockCommand {

	public ProfileCommand() {
		super("profile");
		this.setDescription("Check a player's profile.");
		this.setUsage("/profile <player>");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		User user = null;
		if (args == null || args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
				return true;
			}
			user = UserManager.getUser(((Player) sender).getUniqueId());
		} else {
			Player pTarget = Bukkit.getPlayer(args[0]);
			if (pTarget != null) {
				user = UserManager.getUser(pTarget.getUniqueId());
			}
		}
		if (user == null) {
			sender.sendMessage(ChatColor.YELLOW + "User not found.");
			return true;
		}
		sender.sendMessage(ChatColor.YELLOW.toString() + ChatColor.STRIKETHROUGH + "+------" + ChatColor.DARK_AQUA + " "
				+ user.getPlayerName() + " " + ChatColor.YELLOW + ChatColor.STRIKETHROUGH + "------+\n"
				+ ChatColor.DARK_AQUA + user.getUserClass().getDisplayName() + ChatColor.YELLOW
				+ " of " + ChatColor.DARK_AQUA + user.getAspect().getDisplayName() + "\n"
				+ ChatColor.YELLOW + "Dream planet: " + ChatColor.DARK_AQUA + user.getDreamPlanet().getDisplayName() + "\n"
				+ ChatColor.YELLOW + "Medium planet: " + ChatColor.DARK_AQUA + user.getMediumPlanet().getDisplayName());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (args.length != 1) {
			return ImmutableList.of();
		} else {
			return super.tabComplete(sender, alias, args);
		}
	}
}
