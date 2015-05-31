package co.sblock.commands.entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.machines.utilities.Icon;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * SblockCommand for requesting that a User set themselves as another's server.
 * 
 * @author Jikoo
 */
public class RequestServerCommand extends SblockCommand {

	public RequestServerCommand() {
		super("requestserver");
		this.setDescription("Ask someone to be your Sburb server player!");
		this.setUsage("/requestserver <player>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(Color.BAD + "Who ya gonna call?");
			return true;
		}
		if (sender.getName().equalsIgnoreCase(args[0])) {
			sender.sendMessage(Color.BAD + "Playing with yourself can only entertain you for so long. Find a friend!");
			return true;
		}
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			sender.sendMessage(Color.BAD + "Unknown user!");
			return true;
		}
		OfflineUser u = Users.getGuaranteedUser(p.getUniqueId());
		if (u == null) {
			sender.sendMessage(Color.BAD + p.getName() + " needs to relog before you can do that!");
			p.sendMessage(Color.BAD + "Your data appears to not have been loaded. Please log out and back in!");
			return true;
		}
		if (u.getClient() != null) {
			sender.sendMessage(Color.BAD_PLAYER + u.getPlayerName() + Color.BAD
					+ " appears to have a client already! You'd best find someone else.");
			return true;
		}
		if (!u.getPrograms().contains(Icon.SBURBSERVER.getProgramID())) {
			sender.sendMessage(Color.BAD_PLAYER + u.getPlayerName() + Color.BAD
					+ " does not have the Sburb Server installed!");
			return true;
		}
		if (Users.getInstance().getRequests().containsKey(u.getPlayerName())) {
			sender.sendMessage(Color.BAD_PLAYER + u.getPlayerName() + Color.BAD
					+ " has a pending request to handle already!");
			return true;
		}
		sender.sendMessage(Color.GOOD + "Request sent to " + Color.GOOD_PLAYER + p.getName());
		Users.getInstance().getRequests().put(u.getPlayerName(), "c" + sender.getName());
		u.getPlayer().sendMessage(Color.GOOD_PLAYER + sender.getName() + Color.GOOD
				+ " has requested that you be their server!" + Color.COMMAND
				+ "\n/acceptrequest" + Color.GOOD + " or "
				+ Color.COMMAND + "/declinerequest");
		return true;
	}

	// CHAT: tab-complete arg0 if sender has client, only users with server installed
}
