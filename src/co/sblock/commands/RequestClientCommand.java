package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.machines.utilities.Icon;
import co.sblock.users.OfflineUser;
import co.sblock.users.UserManager;

/**
 * SblockCommand for requesting that a User set themselves as another's client.
 * 
 * @author Jikoo
 */
public class RequestClientCommand extends SblockCommand {

	public RequestClientCommand() {
		super("requestclient");
		this.setDescription("Ask someone to be your Sburb client player!");
		this.setUsage("/requestclient <player>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Who ya gonna call?");
			return true;
		}
		if (sender.getName().equalsIgnoreCase(args[0])) {
			sender.sendMessage(ChatColor.RED + "Playing with yourself can only entertain you for so long. Find a friend!");
			return true;
		}
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			sender.sendMessage(ChatColor.RED + "Unknown user!");
			return true;
		}
		OfflineUser u = UserManager.getGuaranteedUser(p.getUniqueId());
		if (u == null) {
			sender.sendMessage(ChatColor.RED + p.getName() + " needs to relog before you can do that!");
			p.sendMessage(ChatColor.RED + "Your data appears to not have been loaded. Please log out and back in!");
			return true;
		}
		if (u.getServer() != null) {
			sender.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " appears to have a server already! You'd best find someone else.");
			return true;
		}
		if (!u.getPrograms().contains(Icon.SBURBCLIENT.getProgramID())) {
			sender.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " does not have the Sburb Client installed!");
			return true;
		}
		if (UserManager.getUserManager().getRequests().containsKey(u.getPlayerName())) {
			sender.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " has a pending request to handle already!");
			return true;
		}
		sender.sendMessage(ChatColor.YELLOW + "Request sent to " + ChatColor.GREEN + p.getName());
		UserManager.getUserManager().getRequests().put(u.getPlayerName(), "s" + sender.getName());
		u.getPlayer().sendMessage(ChatColor.GREEN + sender.getName() + ChatColor.YELLOW
				+ " has requested that you be their client!" + ChatColor.AQUA
				+ "\n/acceptrequest" + ChatColor.YELLOW + " or "
				+ ChatColor.AQUA + "/declinerequest");
		return true;
	}

	// TODO only tab complete arg0, tab complete only users with client installed, only if sender has server
}
