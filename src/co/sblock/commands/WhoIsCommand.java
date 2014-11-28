package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * SblockCommand for checking a User's stored data.
 * 
 * @author Jikoo
 */
public class WhoIsCommand extends SblockCommand {

	public WhoIsCommand() {
		super("whois");
		this.setDescription("Check data stored for a player.");
		this.setUsage("/whois <exact player>");
		ArrayList<String> aliases = new ArrayList<>();
		aliases.add("profile");
		this.setAliases(aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
			return true;
		}
		if (!sender.hasPermission("group.felt")) {
			User user = null;
			if (args.length == 0) {
				user = UserManager.getUser(((Player) sender).getUniqueId());
			} else {
				Player pTarget = Bukkit.getPlayer(args[0]);
				if (pTarget != null) {
					user = UserManager.getUser(pTarget.getUniqueId());
				} else {
					sender.sendMessage(ChatColor.YELLOW + "User not found.");
					return true;
				}
			}
			StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH)
					.append("+------").append(ChatColor.DARK_AQUA).append(' ').append(user.getPlayerName())
					.append(' ').append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("------+\n");
			sb.append(ChatColor.DARK_AQUA).append(user.getUserClass().getDisplayName()).append(ChatColor.YELLOW)
					.append(" of ").append(ChatColor.DARK_AQUA).append(user.getAspect().getDisplayName()).append('\n');
			sb.append(ChatColor.YELLOW).append("Dream planet: ").append(user.getDreamPlanet().getColor())
					.append(user.getDreamPlanet().getDisplayName()).append('\n');
			sb.append(ChatColor.YELLOW).append("Medium planet: ").append(user.getMediumPlanet().getColor())
					.append(user.getMediumPlanet().getDisplayName());
			return true;
		}
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			// TODO SblockData.getDB().startOfflineLookup(sender, args[0]);
			return true;
		}
		User u = UserManager.getUser(p.getUniqueId());
		sender.sendMessage(u.toString());
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
