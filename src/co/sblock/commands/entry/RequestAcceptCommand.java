package co.sblock.commands.entry;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

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
		if (!Users.getInstance().getRequests().containsKey(sender.getName())) {
			sender.sendMessage(Color.BAD + "You should get someone to /requestserver or /requestclient before attempting to accept!");
			return true;
		}
		String req = Users.getInstance().getRequests().remove(sender.getName());
		OfflineUser u = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		Player p1 = Bukkit.getPlayer(req.substring(1));
		if (p1 == null) {
			sender.sendMessage(Color.BAD_PLAYER + req.substring(1) + Color.BAD + " appears to be offline! Request removed.");
			return true;
		}
		OfflineUser u1 = Users.getGuaranteedUser(p1.getUniqueId());
		if (req.charAt(0) == 'c') {
			u.setClient(u1.getUUID());
			u1.setServer(u.getUUID());
		} else {
			u1.setClient(u.getUUID());
			u.setServer(u1.getUUID());
		}
		sender.sendMessage(Color.GOOD + "Accepted " + Color.GOOD_PLAYER + u1.getPlayerName() + Color.GOOD + "'s request!");
		u1.getPlayer().sendMessage(Color.GOOD_PLAYER + sender.getName() + Color.GOOD + " accepted your request!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
