package co.sblock.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.users.OfflineUser;
import co.sblock.users.UserManager;

/**
 * SblockCommand for accepting a pending server or client request.
 * 
 * @author Jikoo
 */
public class RequestAcceptCommand extends SblockCommand {

	public RequestAcceptCommand() {
		super("acceptrequest");
		this.setDescription("Accept an open request!");
		this.setUsage("/acceptrequest");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (!UserManager.getUserManager().getRequests().containsKey(sender.getName())) {
			sender.sendMessage(ChatColor.RED + "You should get someone to /requestserver or /requestclient before attempting to accept!");
			return true;
		}
		String req = UserManager.getUserManager().getRequests().remove(sender.getName());
		OfflineUser u = UserManager.getGuaranteedUser(((Player) sender).getUniqueId());
		Player p1 = Bukkit.getPlayer(req.substring(1));
		if (p1 == null) {
			sender.sendMessage(ChatColor.GOLD + req.substring(1) + ChatColor.RED + " appears to be offline! Request removed.");
			return true;
		}
		OfflineUser u1 = UserManager.getGuaranteedUser(p1.getUniqueId());
		if (req.charAt(0) == 'c') {
			u.setClient(u1.getUUID());
			u1.setServer(u.getUUID());
		} else {
			u1.setClient(u.getUUID());
			u.setServer(u1.getUUID());
		}
		sender.sendMessage(ChatColor.YELLOW + "Accepted " + ChatColor.GREEN + u1.getPlayerName() + ChatColor.YELLOW + "'s request!");
		u1.getPlayer().sendMessage(ChatColor.GREEN + sender.getName() + ChatColor.YELLOW + " accepted your request!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
