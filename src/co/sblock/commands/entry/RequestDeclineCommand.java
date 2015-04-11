package co.sblock.commands.entry;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockCommand;
import co.sblock.users.Users;

/**
 * 
 * 
 * @author Jikoo
 */
public class RequestDeclineCommand extends SblockCommand {

	public RequestDeclineCommand() {
		super("declinerequest");
		this.setDescription("Say \"no\" to peer pressure!");
		this.setUsage("/declinerequest");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (!Users.getInstance().getRequests().containsKey(sender.getName())) {
			sender.sendMessage(ChatColor.RED + "You vigorously decline... no one."
					+ "\nPerhaps you should get someone to /requestserver or /requestclient first?");
		}
		String name = Users.getInstance().getRequests().remove(sender.getName()).substring(1);
		Player p = Bukkit.getPlayer(name);
		if (p != null) {
			p.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.RED + " has declined your request!");
		}
		sender.sendMessage(ChatColor.RED + "Declined request from " + ChatColor.GOLD + name
				+ ChatColor.RED + "!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
