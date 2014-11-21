package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.channel.Channel;
import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * SblockCommand for forcing a User to change current channel.
 * 
 * @author Jikoo
 */
public class ForceChannelCommand extends SblockCommand {

	public ForceChannelCommand() {
		super("forcechannel");
		this.setDescription("Help people find their way.");
		this.setUsage("/forcechannel <channel> <player>");
		this.setPermission("group.felt");
		this.setPermissionMessage("Try /sc c <channel>");
	}

	/* (non-Javadoc)
	 * @see co.sblock.commands.SblockCommand#execute(org.bukkit.command.CommandSender, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			return false;
		}
		Channel c = ChannelManager.getChannelManager().getChannel(args[0]);
		if (c == null) {
			sender.sendMessage(ChatMsgs.errorInvalidChannel(args[0]));
			return true;
		}
		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(args[1]));
			return true;
		}
		User user = UserManager.getUser(p.getUniqueId());
		user.setCurrent(c);
		sender.sendMessage(ChatColor.GREEN + "Channel forced!");
		return true;
	}

	// TODO tab complete channels for arg 0
}
