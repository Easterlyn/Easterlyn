package co.sblock.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
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
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args == null || args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
		}
		if (sender instanceof Player && !sender.hasPermission("group.felt")) {
			return Sblock.getInstance().getCommandMap().getCommand("profile").execute(sender, label, args);
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
