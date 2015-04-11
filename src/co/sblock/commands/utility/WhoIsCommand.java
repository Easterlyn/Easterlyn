package co.sblock.commands.utility;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.users.Users;

/**
 * SblockCommand for checking a User's stored data.
 * 
 * @author Jikoo
 */
public class WhoIsCommand extends SblockAsynchronousCommand {

	public WhoIsCommand() {
		super("whois");
		this.setAliases("profile");
		this.setDescription("Check data stored for a player.");
		this.setUsage("/whois <player>");
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			return false;
		}
		final UUID uuid = args.length >= 1 ? getUniqueId(args[0]) : ((Player) sender).getUniqueId();
		if (uuid == null) {
			sender.sendMessage(ChatColor.GOLD + args[0] + ChatColor.RED + " has never played on this server.");
			return true;
		}
		if (sender.hasPermission("sblock.felt")) {
			sender.sendMessage(Users.getGuaranteedUser(uuid).getWhois());
		} else {
			sender.sendMessage(Users.getGuaranteedUser(uuid).getProfile());
		}
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
